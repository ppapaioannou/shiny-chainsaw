package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.request.LoginRequest;
import app.rescue.backend.payload.request.RegistrationRequest;
import app.rescue.backend.payload.resposne.AuthenticationResponse;
import app.rescue.backend.service.ImageService;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthController {

    //TODO resend confirmation mail

    private final AuthService authService;
    private final NotificationService notificationService;
    private final ImageService imageService;

    public AuthController(AuthService authService, NotificationService notificationService, ImageService imageService) {
        this.authService = authService;
        this.notificationService = notificationService;
        this.imageService = imageService;
    }


    @PostMapping(path = {"registration/{userRole}", "/ref/registration/{referralToken}/{userRole}"})
    public ResponseEntity<String> register(@RequestParam("request") RegistrationRequest request,
                                           @RequestParam(value = "file", required = false) MultipartFile profileImage,
                                           @PathVariable String userRole,
                                           @PathVariable(required = false) String referralToken) throws IOException {
        User newUser = authService.register(request, userRole.toUpperCase(Locale.ROOT), referralToken);

        //if (profileImage != null) {
        imageService.storeProfileImage(newUser, profileImage);
        //}
        if (referralToken != null) {
            notificationService.sendInvitationCompletedNotification(newUser);
        }
        //if (newUser.getUserRole().equals(Role.ORGANIZATION)) {
        //    //TODO send proximity notification about new organization near
        //}
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PostMapping(path = "login")
    public AuthenticationResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return authService.confirmToken(token);
    }

}
