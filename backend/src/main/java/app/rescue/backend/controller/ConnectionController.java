package app.rescue.backend.controller;

import app.rescue.backend.service.ConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="api/v1/connect")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping(path="with/{userId}")
    public ResponseEntity connectWith(@PathVariable String userId) {
        connectionService.connectWith(userId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping(path="with/{userId}")
    public ResponseEntity acceptConnection(@PathVariable String userId) {
        connectionService.acceptConnection(userId);
        return new ResponseEntity(HttpStatus.OK);
    }
}
