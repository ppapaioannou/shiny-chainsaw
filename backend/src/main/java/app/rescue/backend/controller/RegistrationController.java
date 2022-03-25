package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.RegistrationDto;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping(path = "api/v1/registration")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final NotificationService notificationService;

    public RegistrationController(RegistrationService registrationService, NotificationService notificationService) {
        this.registrationService = registrationService;
        this.notificationService = notificationService;
    }

    //@PostMapping(path = "{userRole}")
    @PostMapping(value = {"{userRole}", "/ref/{referralToken}/{userRole}"})
    public ResponseEntity<String> register(@RequestBody RegistrationDto registrationDto,
                                   @PathVariable String userRole,
                                   @PathVariable(required = false) String referralToken) {
        User newUser = registrationService.register(registrationDto, userRole.toUpperCase(Locale.ROOT), referralToken);
        if (referralToken != null) {
            notificationService.sendInvitationCompleteNotification(newUser);
        }
        return new ResponseEntity<>(newUser.getName(), HttpStatus.OK);

    }

    /*
    @PostMapping(path = "ref/{refToken}/{userRole}")
    public String registerFromInvitation(@RequestBody RegistrationDto request,
                                         @PathVariable String referralToken,
                                         @PathVariable String userRole) {
        return registrationService.registerInvited(request, referralToken, userRole.toUpperCase(Locale.ROOT));
    }
    */


    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }

}
