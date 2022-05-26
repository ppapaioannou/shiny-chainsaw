package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.service.ImageService;
import app.rescue.backend.service.UserService;
import app.rescue.backend.util.AppConstants;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;

    public UserController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping(path = "all")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @SearchSpec Specification<User> specs, Principal principal) {
        return new ResponseEntity<>(userService.getAllUsers(pageNo, pageSize, sortBy, sortDir,
                    Specification.where(specs), principal.getName()), HttpStatus.OK);
    }

    @GetMapping(path = "{userId}")
    public ResponseEntity<UserDto> getSingleUser(@PathVariable String userId) {
        return new ResponseEntity<>(userService.getSingleUser(Long.valueOf(userId)), HttpStatus.OK);
    }

    @PostMapping(path = "ref/{email}")
    public HttpStatus inviteFriend(@PathVariable String email, Principal principal) {
        userService.inviteFriend(email, principal.getName());
        return HttpStatus.OK;
    }

    @PutMapping(path = "update-location")
    public ResponseEntity<String> updateUserLocation(@RequestBody LocationDto request, Principal principal) {
        userService.updateUserLocation(request, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "update-info")
    public ResponseEntity<String> updateUserInfo(@RequestParam("request") UserDto request,
                                                 @RequestParam(value = "file", required = false) MultipartFile profileImage,
                                                 Principal principal) throws IOException {
        userService.updateUserInfo(request, principal.getName());
        imageService.updateProfileImage(userService.getUserByEmail(principal.getName()), profileImage);
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
