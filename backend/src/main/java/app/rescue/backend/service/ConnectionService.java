package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.ConnectionDto;
import app.rescue.backend.repository.ConnectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;

    private final UserService userService;

    private final ImageService imageService;

    public ConnectionService(ConnectionRepository connectionRepository, UserService userService, ImageService imageService) {
        this.connectionRepository = connectionRepository;
        this.userService = userService;
        this.imageService = imageService;
    }

    public Connection connectWith(Long userId, String username) {
        Connection connection = new Connection();

        User user = userService.getUserByEmail(username);
        User connectedToUser = userService.getUserById(userId);

        if (connectionIsPossible(user, connectedToUser)) {
            connection.setUser(user);
            connection.setConnectedToId(userId);
            setConnectionStatus(connection, connectedToUser.getUserRole());
            connectionRepository.save(connection);
        }
        return connection;
    }

    public List<ConnectionDto> getAllConnections(String connectionType, String username) {
        User user = userService.getUserByEmail(username);
        List<Connection> connections;
        boolean getConnectedToUser;
        switch (connectionType) {
            case "friend-requests":
                connections = connectionRepository.getAllByConnectedToIdAndConnectionStatus(user.getId(), "PENDING");
                getConnectedToUser = false;
                break;
            case "followers":
                connections = connectionRepository.getAllByConnectedToIdAndConnectionStatus(user.getId(), "FOLLOWER");
                getConnectedToUser = false;
                break;
            case "friends":
                connections = connectionRepository.getAllByUserAndConnectionStatus(user, "CONNECTED");
                getConnectedToUser = true;
                break;
            case "organizations":
                connections = connectionRepository.getAllByUserAndConnectionStatus(user, "FOLLOWER");
                getConnectedToUser = true;
                break;
            default:
                throw new IllegalStateException("Not a connection type");
        }
        return connections.stream().map(connection -> mapFromConnectionToResponse(connection, getConnectedToUser)).collect(Collectors.toList());
    }

    public String getConnectionStatus(Long connectedToId, String username) {
        User user = userService.getUserByEmail(username);
        Optional<Connection> connection1 = connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId);
        if (connection1.isPresent()) {
            return connection1.get().getConnectionStatus();
        }
        else {
            User connectedToUser = userService.getUserById(connectedToId);
            Optional<Connection> connection2 = connectionRepository.findConnectionByUserAndConnectedToId(connectedToUser, user.getId());
            if (connection2.isPresent()) {
                return "CONNECTION REQUEST";
            }
            else {
                return "NOT CONNECTED";
            }
        }
    }

    public Connection acceptConnection(Long userId, String username) {
        User user = userService.getUserByEmail(username);

        User connectedToUser = userService.getUserById(userId);

        if (alreadyConnected(user, connectedToUser)) {
            throw new IllegalStateException("Users already connected");
        }

        if (!pendingConnection(connectedToUser, user)) {
            throw new IllegalStateException(String.format("No connection request found from user ID:%s",userId));
        }

        if (!user.getUserRole().equals(Role.INDIVIDUAL) && connectedToUser.getUserRole().equals(Role.INDIVIDUAL)) {
            throw new IllegalStateException("This user can not accept connection requests");
        }

        Connection connection = new Connection(user, connectedToUser.getId(), "CONNECTED");

        connectionRepository.save(connection);

        connectionRepository.completeConnection(connectedToUser, user.getId());
        return connection;
    }

    public void declineConnection(Long userId, String username) {
        User user = userService.getUserByEmail(username);

        User connectedToUser = userService.getUserById(userId);

        Optional<Connection> connection = connectionRepository.findConnectionByUserAndConnectedToId(connectedToUser, user.getId());

        connection.ifPresent(connectionRepository::delete);
    }

    public void invitationRegistration(User newUser, User invitedByUser) {
        if (newUser.getUserRole() == Role.INDIVIDUAL && invitedByUser.getUserRole() == Role.INDIVIDUAL) {
            connectionRepository.save(new Connection(newUser, invitedByUser.getId(), "REF-ACCOUNT-DISABLED"));
            connectionRepository.save(new Connection(invitedByUser, newUser.getId(), "REF-PENDING"));
        }
        else if (newUser.getUserRole() == Role.INDIVIDUAL && invitedByUser.getUserRole() == Role.ORGANIZATION) {
            connectionRepository.save(new Connection(newUser, invitedByUser.getId(), "REF-FOLLOWER"));
        }
    }

    public void completeRefConnection(User user) {
        Optional<Connection> connection = connectionRepository.findRefPendingConnection(user);
        if (connection.isPresent()) {
            User connectedToUser = userService.getUserById(connection.get().getConnectedToId());
            if (connectedToUser.getUserRole().equals(Role.INDIVIDUAL)) {
                connectionRepository.completeConnection(user, connectedToUser.getId());
                connectionRepository.completeConnection(connectedToUser, user.getId());
            }
            else if (connectedToUser.getUserRole().equals(Role.ORGANIZATION)) {
                connectionRepository.completeRefOrgConnection(user);
            }
        }
    }

    public void deleteConnection(Long connectedToUserId, String userName) {
        User user = userService.getUserByEmail(userName);
        User connectedToUser = userService.getUserById(connectedToUserId);

        Optional<Connection> connection1 = connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToUserId);
        if (connection1.isPresent()) {
            connectionRepository.delete(connection1.get());
            Optional<Connection> connection2 = connectionRepository.findConnectionByUserAndConnectedToId(connectedToUser, user.getId());
            connection2.ifPresent(connectionRepository::delete);
        }
    }

    public List<Connection> findConnectionsByUser(User user) {
        return connectionRepository.findConnectionsByUser(user);
    }

    private boolean connectionIsPossible(User user, User connectedToUser) {
        if (user.equals(connectedToUser)) {
            throw new IllegalStateException("You cannot connect with yourself");
        }

        if (user.getUserRole().equals(Role.ORGANIZATION)) {
            throw new IllegalStateException("Organizations can't make connection requests");
        }

        if (alreadyConnected(user, connectedToUser) || alreadyConnected(connectedToUser, user)) {
            throw new IllegalStateException("Users are connected or there is already a connection request in progress");
        }
        return true;
    }

    private boolean alreadyConnected(User user, User connectedToUser) {
        Optional<Connection> connection = connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToUser.getId());
        return connection.isPresent();
    }

    private void setConnectionStatus(Connection connection, Role userRole) {
        if (userRole.equals(Role.INDIVIDUAL)) {
            connection.setConnectionStatus("PENDING");
        }
        else if (userRole.equals(Role.ORGANIZATION)) {
            connection.setConnectionStatus("FOLLOWER");
        }
        else {
            throw new IllegalStateException("Not an User Role");
        }
    }

    private ConnectionDto mapFromConnectionToResponse(Connection connection, boolean getConnectedToUser) {
        ConnectionDto response = new ConnectionDto();
        User user;

        if (getConnectedToUser) {
            user = userService.getUserById(connection.getConnectedToId());
        }
        else {
            user = connection.getUser();
        }

        response.setUserId(user.getId());
        response.setName(user.getName());
        if (user.getUserRole().equals(Role.INDIVIDUAL)) {
            response.setLastName(user.getIndividualInformation().getLastName());
        }

        Image profileImage = imageService.getProfileImage(user);
        response.setProfileImage(imageService.createFileDownloadUri(profileImage));

        return response;
    }

    private boolean pendingConnection(User user, User connectedToUser) {
        Optional<Connection> connection = connectionRepository.findPendingConnection(user, connectedToUser.getId());
        return connection.isPresent();
    }

}
