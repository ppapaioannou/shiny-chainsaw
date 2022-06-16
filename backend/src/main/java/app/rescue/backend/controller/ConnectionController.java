package app.rescue.backend.controller;

import app.rescue.backend.model.Connection;
import app.rescue.backend.payload.ConnectionDto;
import app.rescue.backend.service.ConnectionService;
import app.rescue.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
        Connection connection = connectionService.connectWith(userId, principal.getName());
        notificationService.sendConnectionRequestNotification(connection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path="{connectionType}")
    public ResponseEntity<List<ConnectionDto>> getAllConnections(@PathVariable String connectionType, Principal principal) {
        return new ResponseEntity<>(connectionService.getAllConnections(connectionType, principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path="status/{connectedToId}")
    public ResponseEntity<String> getConnectionStatus(@PathVariable Long connectedToId, Principal principal) {
        return new ResponseEntity<>(connectionService.getConnectionStatus(connectedToId, principal.getName()), HttpStatus.OK);
    }

    @PutMapping(path="accept/{userId}")
    public ResponseEntity<String> acceptConnection(@PathVariable Long userId, Principal principal) {
        Connection connection = connectionService.acceptConnection(userId, principal.getName());
        notificationService.sendConnectionAcceptedNotification(connection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path="decline/{userId}")
    public ResponseEntity<String> declineConnection(@PathVariable Long userId, Principal principal) {
        connectionService.declineConnection(userId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path="delete/{userId}")
    public ResponseEntity<String> deleteConnection(@PathVariable Long userId, Principal principal) {
        connectionService.deleteConnection(userId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
