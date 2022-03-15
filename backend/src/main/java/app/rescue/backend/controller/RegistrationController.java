package app.rescue.backend.controller;

import app.rescue.backend.dto.RegistrationDto;
import app.rescue.backend.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping(path = "individual")
    public String registerIndividual(@RequestBody RegistrationDto request) {
        return registrationService.register(request, "INDIVIDUAL");
    }

    @PostMapping(path = "organization")
    public String registerOrganization(@RequestBody RegistrationDto request) {
        return registrationService.register(request, "ORGANIZATION");
    }


    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }

}
