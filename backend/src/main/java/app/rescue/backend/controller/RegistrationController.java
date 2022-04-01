package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.request.RegistrationRequest;
//import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping(path = "api/v1/registration")
public class RegistrationController {

    //TODO resend confirmation mail

    private final RegistrationService registrationService;
    private final NotificationService notificationService;

    public RegistrationController(RegistrationService registrationService, NotificationService notificationService) {
        this.registrationService = registrationService;
        this.notificationService = notificationService;
    }


    @PostMapping(path = {"{userRole}", "/ref/{referralToken}/{userRole}"})
    public ResponseEntity<String> register(@RequestBody RegistrationRequest registrationDto, @PathVariable String userRole,
                                           @PathVariable(required = false) String referralToken) {
        User newUser = registrationService.register(registrationDto, userRole.toUpperCase(Locale.ROOT), referralToken);
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
        return registrationService.confirmToken(token);
    }

}
