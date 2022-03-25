package app.rescue.backend.service;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public ConnectionService(ConnectionRepository connectionRepository, UserService userService, UserRepository userRepository) {
        this.connectionRepository = connectionRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Connection connectWith(String userId) {
        //TODO check if already connected
        Connection connection = new Connection();
        connection.setUser(userRepository.findUserByEmail(userService.getCurrentUser()));

        Optional<User> connectedToUser = userRepository.findById(Long.valueOf(userId));

        if (connectedToUser.isPresent()) {
            User user = connectedToUser.get();
            connection.setConnectedTo(user);
            if (user.getUserRole() == Role.INDIVIDUAL) {
                //TODO make connectionStatus to an ENUM
                connection.setConnectionStatus("PENDING");
            }
            else if (user.getUserRole() == Role.ORGANIZATION) {
                connection.setConnectionStatus("FOLLOWER");
            }
            else {
                throw new IllegalStateException("not a user");
            }
            connectionRepository.save(connection);
        }
        else {
            throw new IllegalStateException("User does not exist");
        }

        return connection;
    }

    public Connection acceptConnection(String userId) {
        Connection connection = new Connection();

        User loggedInUser = userRepository.findUserByEmail(userService.getCurrentUser());
        connection.setUser(loggedInUser);

        Optional<User> connectedToUser = userRepository.findById(Long.valueOf(userId));
        if (connectedToUser.isPresent()) {
            User user = connectedToUser.get();
            connection.setConnectedTo(user);
            connection.setConnectionStatus("CONNECTED");

            connectionRepository.save(connection);

            connectionRepository.connect(user, loggedInUser);
        }
        else {
            throw new IllegalStateException("User does not exist");
        }
        return connection;
    }

    public void connect(User userOne, User userTwo) {
        Connection connectionOne = new Connection(userOne, userTwo, "CONNECTED");
        connectionRepository.save(connectionOne);

        Connection connectionTwo = new Connection(userTwo, userOne, "CONNECTED");
        connectionRepository.save(connectionTwo);
    }
}
