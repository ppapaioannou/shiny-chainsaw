package app.rescue.backend.service;

import app.rescue.backend.model.*;
//import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.payload.request.UserLocationRequest;
import app.rescue.backend.repository.IndividualInformationRepository;
import app.rescue.backend.repository.OrganizationInformationRepository;
import app.rescue.backend.repository.UserRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "user with email %s not found";

    private final UserRepository userRepository;
    private final IndividualInformationRepository individualInformationRepository;
    private final OrganizationInformationRepository organizationInformationRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ConfirmationTokenService confirmationTokenService;
    private final LocationService locationService;

    public UserService(UserRepository userRepository, IndividualInformationRepository individualInformationRepository,
                       OrganizationInformationRepository organizationInformationRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       ConfirmationTokenService confirmationTokenService, LocationService locationService) {
        this.userRepository = userRepository;
        this.individualInformationRepository = individualInformationRepository;
        this.organizationInformationRepository = organizationInformationRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.locationService = locationService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(User newUser) {
        Optional<User> userExists = userRepository.findByEmail(newUser.getEmail());
        if (userExists.isPresent()) {
            if (userExists.get().isEnabled()) {
                throw new IllegalStateException("User with that email already exists");
            }
            userRepository.delete(userExists.get());
        }

        String encodedPassword = bCryptPasswordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        userRepository.save(newUser);

        if (newUser.getUserRole().equals(Role.INDIVIDUAL)) {
            individualInformationRepository.setIndividualInformationUser(newUser.getIndividualInformation(), newUser);
        }
        else if (newUser.getUserRole().equals(Role.ORGANIZATION)) {
            organizationInformationRepository.setOrganizationInformationUser(newUser.getOrganizationInformation(), newUser);
        }

        ConfirmationToken confirmationToken = createConfirmationToken(newUser);

        return confirmationToken.getToken();
    }

    public void enableUser(String email) {
        userRepository.enableUser(email);
    }

    public void updateUserLocation(UserLocationRequest request, String userName) {
        User user = getUserByEmail(userName);
        Geometry userLocation = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            double diameterInMeters = Double.parseDouble(request.getDiameterInMeters());
            userLocation = locationService.userLocationToCircle(latitude, longitude, diameterInMeters);
        }
        user.setLocation(userLocation);
        userRepository.save(user);

    }

    public void deleteUserAccount(String userName) {
        User user = getUserByEmail(userName);
        userRepository.delete(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new IllegalStateException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(String.format("User with ID:%s does not exist", id)));
    }

    public boolean tryToFindUserById(Long id) {
        return userRepository.findById(id).isPresent();
    }

    public User findByReferralToken(String referralToken) {
        return userRepository.findByReferralToken(referralToken).orElseThrow(() ->
                new IllegalStateException("Not a referral token"));
    }

    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    private ConfirmationToken createConfirmationToken(User newUser) {
        ConfirmationToken confirmationToken = new ConfirmationToken(newUser);
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return confirmationToken;
    }








/*
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        else {
            throw new IllegalStateException("No user logged in");
        }
    }

    public List<UserResponse> getAllUsers(String userRole) {
        List<User> users = userRepository.findAllByUserRole(Role.valueOf(userRole));
        return users.stream().map(this::mapFromUserToResponse).collect(Collectors.toList());
    }

    public UserResponse getSingleUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return mapFromUserToResponse(user.get());
        }
        else {
            throw new IllegalStateException("User does not exist.");
        }

    }
*/
    /*
    public void updateUserInfo(RegistrationDto userUpdate, Long userId) {
        //TODO make sure the user is updating their own info
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getUserRole().equals(Role.INDIVIDUAL)) {
                updateIndividualInfo(user, userUpdate);
            }
            else if (user.getUserRole().equals(Role.ORGANIZATION)) {
                updateOrganizationInfo(user, userUpdate);
            }
        }
        else {
            throw new IllegalStateException("User does not exist.");
        }
    }

    private void updateIndividualInfo(User user, RegistrationDto userUpdate) {
        user.setName(userUpdate.getName());
        user.setPhoneNumber(userUpdate.getPhoneNumber());
        user.setDescription(userUpdate.getDescription());
        ((Individual) user).setLastName(userUpdate.getLastName());
        if (userUpdate.getDateOfBirth() != null) {
            ((Individual) user).setDateOfBirth(Date.valueOf(userUpdate.getDateOfBirth()));
        }


        userRepository.save(user);


    }

     private void updateOrganizationInfo(User user, RegistrationDto userUpdate) {
    }
    */
/*
    public void deleteUserAccount() {
        //String email = getCurrentUserEmail();
        User user = userRepository.findUserByEmail(getCurrentUserEmail());
        userRepository.delete(user);
    }
*/


/*
    private UserResponse mapFromUserToResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        if (user.getUserRole().equals(Role.INDIVIDUAL)) {
            userResponse.setLastName(((Individual) user).getLastName());
        }
        //else if (user.getUserRole().equals(Role.ORGANIZATION)) {
            //userResponse.setLastName("");
        //}

        return userResponse;
    }
*/
}
