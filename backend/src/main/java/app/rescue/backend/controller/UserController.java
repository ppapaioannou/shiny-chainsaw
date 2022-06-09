package app.rescue.backend.controller;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.service.ImageService;
import app.rescue.backend.service.UserService;
import app.rescue.backend.utility.AppConstants;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
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

    @PostMapping(path = "ref/{email}")
    public HttpStatus inviteFriend(@PathVariable String email, Principal principal) throws MessagingException {
        userService.inviteFriend(email, principal.getName());
        return HttpStatus.OK;
    }

    @GetMapping(path = "all")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @SearchSpec Specification<User> specs) {
        // create Pageable instance
        Pageable pageable = AppConstants.createPageableRequest(pageNo, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(userService.getAllUsers(Specification.where(specs), pageable), HttpStatus.OK);
    }

    @GetMapping(path = "{userId}")
    public ResponseEntity<UserDto> getSingleUser(@PathVariable String userId) {
        return new ResponseEntity<>(userService.getSingleUser(Long.valueOf(userId)), HttpStatus.OK);
    }

    @PutMapping(path = "update-location")
    public ResponseEntity<String> updateUserLocation(@RequestBody LocationDto request, Principal principal) {
        userService.updateUserLocation(request, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "update-info")
    public ResponseEntity<String> updateUserInfo(@RequestParam("payload") UserDto request,
                                                 @RequestParam(value = "file", required = false) MultipartFile profileImage,
                                                 Principal principal) throws IOException {
        userService.updateUserInfo(request, principal.getName());
        imageService.updateProfileImage(userService.getUserByEmail(principal.getName()), profileImage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO @PutMapping(path = "change-password") changePassword(@RequestBody Principal principal)

    @DeleteMapping(path = "delete")
    public ResponseEntity<String> deleteAccount(Principal principal) {
        userService.deleteAccount(principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
