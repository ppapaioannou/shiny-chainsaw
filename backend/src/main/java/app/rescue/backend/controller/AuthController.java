package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.RegistrationDto;
import app.rescue.backend.payload.LoginDto;
import app.rescue.backend.payload.AuthenticationDto;
import app.rescue.backend.service.ImageService;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final NotificationService notificationService;
    private final ImageService imageService;

    public AuthController(AuthService authService, NotificationService notificationService, ImageService imageService) {
        this.authService = authService;
        this.notificationService = notificationService;
        this.imageService = imageService;
    }


    @PostMapping(path = {"register/{userRole}", "/ref/register/{referralToken}/{userRole}"})
    public ResponseEntity<String> register(@RequestParam("payload") RegistrationDto request,
                                           @RequestParam(value = "file", required = false) MultipartFile profileImage,
                                           @PathVariable String userRole,
                                           @PathVariable(required = false) String referralToken) throws IOException, MessagingException {
        User newUser = authService.register(request, userRole.toUpperCase(Locale.ROOT));

        imageService.storeProfileImage(newUser, profileImage);

        if (referralToken != null) {
            authService.invitationRegistration(newUser, referralToken);
            notificationService.sendInvitationCompletedNotification(newUser);
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PostMapping(path = "login")
    public AuthenticationDto login(@RequestBody LoginDto request) {
        return authService.login(request);
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return authService.confirmToken(token);
    }

}
