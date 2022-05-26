package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.ImageRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;

    private final UserService userService;

    private final ImageRepository imageRepository;

    public ConnectionService(ConnectionRepository connectionRepository, UserService userService, ImageRepository imageRepository) {
        this.connectionRepository = connectionRepository;
        this.userService = userService;
        this.imageRepository = imageRepository;
    }

    public Connection connectWith(Long userId, String userName) {
        Connection connection = new Connection();

        User user = userService.getUserByEmail(userName);

        User connectedTo = userService.getUserById(userId);

        if (user.equals(connectedTo)) {
            throw new IllegalStateException("You cannot connect with yourself");
        }

        if (user.getUserRole().equals(Role.ORGANIZATION)) {
            throw new IllegalStateException("Organizations can't make connection requests");
        }

        if (alreadyConnected(user, connectedTo) ||
                pendingConnection(user, connectedTo) || pendingConnection(connectedTo, user)) {
            throw new IllegalStateException("Users are connected or there is already a connection request in progress");
        }

        connection.setUser(user);
        connection.setConnectedToId(connectedTo.getId());

        if (connectedTo.getUserRole().equals(Role.INDIVIDUAL)) {
            connection.setConnectionStatus("PENDING");
        }
        else if (connectedTo.getUserRole().equals(Role.ORGANIZATION)) {
            connection.setConnectionStatus("FOLLOWER");
        }
        connectionRepository.save(connection);
        return connection;
    }
    
    public List<UserDto> getAllFriendRequests(String userName) {
        User user = userService.getUserByEmail(userName);
        List<Connection> friendRequests = connectionRepository.findAllFriendRequests(user.getId());
        return friendRequests.stream().map(this::mapFromConnectionToResponse).collect(Collectors.toList());
    }

    public List<UserDto> getAllFriends(String userName) {
        User user = userService.getUserByEmail(userName);
        List<Connection> friends = connectionRepository.findAllFriends(user.getId());
        return friends.stream().map(this::mapFromConnectionToResponse).collect(Collectors.toList());
    }

    public List<UserDto> getAllOrganizations(String userName) {
        User user = userService.getUserByEmail(userName);
        List<Connection> organizations = connectionRepository.findAllOrganizations(user);
        return organizations.stream().map(this::mapFromConnectionToResponse).collect(Collectors.toList());
    }

    public List<UserDto> getAllFollowers(String userName) {
        User user = userService.getUserByEmail(userName);
        List<Connection> followers = connectionRepository.findAllFollowers(user.getId());
        return followers.stream().map(this::mapFromFollowerToResponse).collect(Collectors.toList());
    }

    public Boolean  isConnectedTo(Long connectedToId, String userName) {
        User user = userService.getUserByEmail(userName);
        return connectionRepository.existsByUserAndConnectedToId(user, connectedToId);
    }

    public Connection acceptConnection(Long userId, String userName) {
        User user = userService.getUserByEmail(userName);

        User connectedTo = userService.getUserById(userId);

        if (alreadyConnected(user, connectedTo)) {
            throw new IllegalStateException("Users already connected");
        }

        if (!pendingConnection(connectedTo, user)) {
            throw new IllegalStateException(String.format("No connection request found from user ID:%s",userId));
        }

        if (!user.getUserRole().equals(Role.INDIVIDUAL) && connectedTo.getUserRole().equals(Role.INDIVIDUAL)) {
            throw new IllegalStateException("This user can not accept connection requests");
        }

        Connection connection = new Connection(user, connectedTo.getId(), "CONNECTED");

        connectionRepository.save(connection);

        connectionRepository.completeConnection(connectedTo, user.getId());
        return connection;
    }

    public void invitationRegistration(User user, User invitedByUser) {
        if (user.getUserRole() == Role.INDIVIDUAL && invitedByUser.getUserRole() == Role.INDIVIDUAL) {
            connectionRepository.save(new Connection(user, invitedByUser.getId(), "ACCOUNT-DISABLED"));
            connectionRepository.save(new Connection(invitedByUser, user.getId(), "REF-PENDING"));
        }
        else if (user.getUserRole() == Role.INDIVIDUAL && invitedByUser.getUserRole() == Role.ORGANIZATION) {
            connectionRepository.save(new Connection(user, invitedByUser.getId(), "REF-FOLLOWER"));
        }
    }

    public void completeRefConnection(User user) {
        Optional<Connection> connection = connectionRepository.findByUser(user);
        if (connection.isPresent() && !connection.get().getConnectionStatus().equals("REF-PENDING")) {
            User connectedTo = userService.getUserById(connection.get().getConnectedToId());
            if (connectedTo.getUserRole().equals(Role.INDIVIDUAL)) {
                connectionRepository.completeConnection(user, connectedTo.getId());
                connectionRepository.completeConnection(connectedTo, user.getId());
            }
            else if (connectedTo.getUserRole().equals(Role.ORGANIZATION)) {
                connectionRepository.completeRefOrgConnection(user);
            }
        }
    }

    public void deleteConnection(Long userId, String userName) {
        User user = userService.getUserByEmail(userName);
        User connectedToUser = userService.findById(userId);
        Optional<Connection> connection1 = connectionRepository.getConnectionByUserAndConnectedToId(user, userId);
        if (connection1.isPresent()) {
            connectionRepository.delete(connection1.get());
            Optional<Connection> connection2 = connectionRepository.getConnectionByUserAndConnectedToId(connectedToUser, user.getId());
            connection2.ifPresent(connectionRepository::delete);

        }
    }

    public Collection<Connection> findConnectionsByUser(User user) {
        return connectionRepository.findConnectionsByUser(user);
    }

    private boolean alreadyConnected(User user, User connectedTo) {
        Optional<Connection> connection = connectionRepository.getConnectionByUserAndConnectedToId(user, connectedTo.getId());
        return connection.isPresent();
    }

    private boolean pendingConnection(User user, User connectedTo) {
        Optional<Connection> connection = connectionRepository.getPendingConnection(user, connectedTo.getId());
        return connection.isPresent();
    }

    private UserDto mapFromConnectionToResponse(Connection connection) {
        UserDto response = new UserDto();
        User user = userService.getUserById(connection.getUser().getId());

        Image profileImage;
        User connectedToUser = userService.getUserById(connection.getConnectedToId());
        if (connectedToUser.getUserRole().equals(Role.INDIVIDUAL)) {
            response.setId(user.getId().toString());
            response.setName(user.getName());
            response.setLastName(user.getIndividualInformation().getLastName());
            profileImage = imageRepository.findByUserAndProfileImage(user, true);
        }
        else if (connectedToUser.getUserRole().equals(Role.ORGANIZATION)) {
            response.setId(connectedToUser.getId().toString());
            response.setName(connectedToUser.getName());
            profileImage = imageRepository.findByUserAndProfileImage(connectedToUser, true);
        }
        else {
            throw new IllegalStateException("connection profile Image error");
        }


        //Image profileImage = imageRepository.findByUserAndProfileImage(user, true);
        String profileImageLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/image/")
                .path(String.valueOf(profileImage.getId()))
                .toUriString();
        response.setProfileImage(profileImageLink);



        return response;
    }
    //TODO THIS NEEDS TO BE BETTER

    private UserDto mapFromFollowerToResponse(Connection connection) {
        UserDto response = new UserDto();
        //User organization = userService.getUserById(connection.getConnectedToId());

        User follower = userService.getUserById(connection.getUser().getId());

        response.setId(follower.getId().toString());
        response.setName(follower.getName());
        response.setLastName(follower.getIndividualInformation().getLastName());
        Image profileImage = imageRepository.findByUserAndProfileImage(follower, true);
        /*

        if (follower.getUserRole().equals(Role.INDIVIDUAL)) {
            response.setId(follower.getId().toString());
            response.setName(follower.getName());
            response.setLastName(follower.getIndividualInformation().getLastName());
            profileImage = imageRepository.findByUserAndProfileImage(follower, true);
        }
        else if (follower.getUserRole().equals(Role.ORGANIZATION)) {
            response.setId(follower.getId().toString());
            response.setName(follower.getName());
            profileImage = imageRepository.findByUserAndProfileImage(follower, true);
        }
        else {
            throw new IllegalStateException("connection profile Image error");
        }
        */


        //Image profileImage = imageRepository.findByUserAndProfileImage(user, true);
        String profileImageLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/image/")
                .path(String.valueOf(profileImage.getId()))
                .toUriString();
        response.setProfileImage(profileImageLink);



        return response;
    }

}
