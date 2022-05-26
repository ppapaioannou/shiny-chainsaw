package app.rescue.backend.controller;


import app.rescue.backend.model.Connection;
import app.rescue.backend.payload.ConnectionDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.service.ConnectionService;
import app.rescue.backend.service.NotificationService;
import org.springframework.data.jpa.domain.Specification;
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
        //TODO Connection or Connection Response
        Connection connection = connectionService.connectWith(userId, principal.getName());
        notificationService.sendConnectionRequestNotification(connection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path="friend-requests")
    public ResponseEntity<List<UserDto>> getAllFriendRequests(Principal principal) {
        return new ResponseEntity<>(connectionService.getAllFriendRequests(principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path="friends")
    public ResponseEntity<List<UserDto>> getAllFriends(Principal principal) {
        return new ResponseEntity<>(connectionService.getAllFriends(principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path="organizations")
    public ResponseEntity<List<UserDto>> getAllOrganizations(Principal principal) {
        return new ResponseEntity<>(connectionService.getAllOrganizations(principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path="followers")
    public ResponseEntity<List<UserDto>> getAllFollowers(Principal principal) {
        return new ResponseEntity<>(connectionService.getAllFollowers(principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path="is-connected-to/{connectedToId}")
    public ResponseEntity<Boolean> getAllFriends(@PathVariable Long connectedToId, Principal principal) {
        return new ResponseEntity<>(connectionService.isConnectedTo(connectedToId, principal.getName()), HttpStatus.OK);
    }

    @PutMapping(path="accept/{userId}")
    public ResponseEntity<String> acceptConnection(@PathVariable Long userId, Principal principal) {
        Connection connection = connectionService.acceptConnection(userId, principal.getName());
        notificationService.sendConnectionAcceptedNotification(connection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path="delete/{userId}")
    public ResponseEntity<String> deleteConnection(@PathVariable Long userId, Principal principal) {
        connectionService.deleteConnection(userId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
