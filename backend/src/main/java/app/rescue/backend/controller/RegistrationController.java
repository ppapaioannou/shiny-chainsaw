package app.rescue.backend.controller;

import app.rescue.backend.dto.RegistrationDto;
import app.rescue.backend.service.RegistrationService;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping(path = "api/v1/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    //@PostMapping(path = "{userRole}")
    @PostMapping(value = {"{userRole}", "/ref/{referralToken}/{userRole}"})
    public String register(@RequestBody RegistrationDto request,
                           @PathVariable String userRole,
                           @PathVariable(required = false) String referralToken) {
        return registrationService.register(request, userRole.toUpperCase(Locale.ROOT), referralToken);

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
