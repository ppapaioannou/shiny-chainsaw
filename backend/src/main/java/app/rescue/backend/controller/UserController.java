package app.rescue.backend.controller;

import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.request.UserLocationRequest;
import app.rescue.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping(path = "update-location")
    public ResponseEntity<String> updateUserLocation(@RequestBody LocationDto request, Principal principal) {
        userService.updateUserLocation(request, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "delete")
    public ResponseEntity<String> deleteUserAccount(Principal principal) {
        userService.deleteUserAccount(principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
/*
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "individuals")
    public ResponseEntity<List<UserResponse>> getAllIndividuals() {
        return new ResponseEntity<>(userService.getAllUsers("INDIVIDUAL"), HttpStatus.OK);

    }

    @GetMapping(path = "organizations")
    public ResponseEntity<List<UserResponse>> getAllOrganizations() {
        return new ResponseEntity<>(userService.getAllUsers("ORGANIZATION"), HttpStatus.OK);
    }

    @GetMapping(path = "{userId}")
    public ResponseEntity<UserResponse> getSingleUser(@PathVariable String userId) {
        return new ResponseEntity<>(userService.getSingleUser(Long.valueOf(userId)), HttpStatus.OK);
    }

    @PutMapping(path = "update/{userId}")
    public ResponseEntity<String> updateUserInfo(@RequestBody RegistrationDto userUpdate, @PathVariable String userId) {
        //userService.updateUserInfo(userUpdate, Long.valueOf(userId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "delete")
    public ResponseEntity<String> deleteUserAccount() {
        userService.deleteUserAccount();
        return new ResponseEntity<>(HttpStatus.OK);
    }
*/
}
