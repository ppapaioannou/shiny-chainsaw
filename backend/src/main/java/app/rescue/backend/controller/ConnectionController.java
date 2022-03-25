package app.rescue.backend.controller;

import app.rescue.backend.model.Connection;
import app.rescue.backend.service.ConnectionService;
import app.rescue.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="api/v1/connect")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final NotificationService notificationService;

    public ConnectionController(ConnectionService connectionService, NotificationService notificationService) {
        this.connectionService = connectionService;
        this.notificationService = notificationService;
    }

    @PostMapping(path="with/{userId}")
    public ResponseEntity<String> connectWith(@PathVariable String userId) {
        Connection connection = connectionService.connectWith(userId);
        notificationService.sendConnectionNotification(connection);
        return new ResponseEntity<>(connection.toString(), HttpStatus.OK);
    }

    @PutMapping(path="with/{userId}")
    public ResponseEntity<String> acceptConnection(@PathVariable String userId) {
        Connection connection = connectionService.acceptConnection(userId);
        notificationService.sendConnectionAcceptedNotification(connection);
        return new ResponseEntity<>(connection.toString(), HttpStatus.OK);
    }
}
