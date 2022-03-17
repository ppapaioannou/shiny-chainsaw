package app.rescue.backend.service;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.UserRepository;
import org.springframework.stereotype.Service;


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

    public void connectWith(String userId) {
        Connection connection = new Connection();
        connection.setUser(userRepository.findUserByEmail(userService.getCurrentUser()));

        User connectedToUser = userRepository.findById(Long.valueOf(userId)).get();
        connection.setConnectedTo(connectedToUser);

        //if (connectedToUser.isPresent()) {
        //    connection.setConnectedTo(connectedToUser.get());
        //}
        if (connectedToUser.getUserRole() == Role.INDIVIDUAL) {
            //TODO make connectionStatus to an ENUM
            connection.setConnectionStatus("PENDING");
        }
        else if (connectedToUser.getUserRole() == Role.ORGANIZATION) {
            connection.setConnectionStatus("FOLLOWER");
        }
        else {
            throw new IllegalStateException("not a user");
        }

        connectionRepository.save(connection);



    }

    public void acceptConnection(String userId) {
        Connection connection = new Connection();

        User loggedInUser = userRepository.findUserByEmail(userService.getCurrentUser());
        connection.setUser(loggedInUser);

        User connectedToUser = userRepository.findById(Long.valueOf(userId)).get();
        connection.setConnectedTo(connectedToUser);

        connection.setConnectionStatus("CONNECTED");

        connectionRepository.save(connection);

        connectionRepository.connect(connectedToUser, loggedInUser);
    }
}
