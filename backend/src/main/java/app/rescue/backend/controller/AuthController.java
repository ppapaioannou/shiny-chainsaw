package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.request.RegistrationRequest;
//import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthController {

    //TODO resend confirmation mail

    private final AuthService authService;
    private final NotificationService notificationService;

    public AuthController(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        this.notificationService = notificationService;
    }


    @PostMapping(path = {"registration/{userRole}", "/ref/registration/{referralToken}/{userRole}"})
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request, @PathVariable String userRole,
                                           @PathVariable(required = false) String referralToken) {
        User newUser = authService.register(request, userRole.toUpperCase(Locale.ROOT), referralToken);
        if (referralToken != null) {

            notificationService.sendInvitationCompletedNotification(newUser);
        }
        //if (newUser.getUserRole().equals(Role.ORGANIZATION)) {
        //    //TODO send proximity notification about new organization near
        //}
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return authService.confirmToken(token);
    }

}
