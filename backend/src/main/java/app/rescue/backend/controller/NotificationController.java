package app.rescue.backend.controller;

import app.rescue.backend.payload.NotificationDto;
import app.rescue.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotifications(Principal principal) {
        return new ResponseEntity<>(notificationService.getAllNotifications(principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path = "unread")
    public ResponseEntity<Integer> getNumberOfUnreadNotifications(Principal principal) {
        return new ResponseEntity<>(notificationService.getNumberOfUnreadNotifications(principal.getName()), HttpStatus.OK);
    }

    @PutMapping(path = "{notificationId}")
    public ResponseEntity<String> readNotification(@PathVariable Long notificationId, Principal principal) {
        notificationService.readNotification(notificationId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId, Principal principal) {
        notificationService.deleteNotification(notificationId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
