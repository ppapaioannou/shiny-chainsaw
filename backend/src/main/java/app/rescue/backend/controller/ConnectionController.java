package app.rescue.backend.controller;


import app.rescue.backend.model.Connection;
import app.rescue.backend.service.ConnectionService;
import app.rescue.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(path="api/v1/connection")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final NotificationService notificationService;

    public ConnectionController(ConnectionService connectionService, NotificationService notificationService) {
        this.connectionService = connectionService;
        this.notificationService = notificationService;
    }

    @PostMapping(path="with/{userId}")
    public ResponseEntity<String> connectWith(@PathVariable Long userId, Principal principal) {
        //TODO Connection or Connection Response
        Connection connection = connectionService.connectWith(userId, principal.getName());
        notificationService.sendConnectionRequestNotification(connection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path="accept/{userId}")
    public ResponseEntity<String> acceptConnection(@PathVariable Long userId, Principal principal) {
        Connection connection = connectionService.acceptConnection(userId, principal.getName());
        notificationService.sendConnectionAcceptedNotification(connection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
