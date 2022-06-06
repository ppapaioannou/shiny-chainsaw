package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.repository.IndividualInformationRepository;
import app.rescue.backend.repository.OrganizationInformationRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.utility.EmailSender;
import app.rescue.backend.utility.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final IndividualInformationRepository individualInformationRepository;
    private final OrganizationInformationRepository organizationInformationRepository;

    private final PasswordEncoder passwordEncoder;

    private final ConfirmationTokenService confirmationTokenService;
    private final LocationService locationService;
    private final ImageService imageService;

    private final EmailSender emailSender;
    private final EmailValidator emailValidator;

    public UserService(UserRepository userRepository, IndividualInformationRepository individualInformationRepository,
                       OrganizationInformationRepository organizationInformationRepository,
                       PasswordEncoder passwordEncoder,
                       ConfirmationTokenService confirmationTokenService, LocationService locationService,
                       ImageService imageService, EmailSender emailSender, EmailValidator emailValidator) {
        this.userRepository = userRepository;
        this.individualInformationRepository = individualInformationRepository;
        this.organizationInformationRepository = organizationInformationRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.locationService = locationService;
        this.imageService = imageService;
        this.emailSender = emailSender;
        this.emailValidator = emailValidator;
    }

    public String signUpUser(User newUser) {
        if (userRepository.existsByEmail(newUser.getEmail())) {
            if (userRepository.existsByEmailAndEnabled(newUser.getEmail(), true)) {
                throw new IllegalStateException("User with that email already exists");
            }
            userRepository.delete(newUser);
        }
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
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

    public void inviteFriend(String email, String username) {
        boolean isValidEmail = emailValidator.check(email);

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }
        User user = getUserByEmail(username);
        String name = user.getName();
        String confirmationLink = "http://localhost:4200/register/individual/ref/"+ user.getReferralToken().strip();

        emailSender.send(email, name, confirmationLink);
    }

    public List<UserDto> getAllUsers(Specification<User> specs, Pageable pageable) {
        Page<User> users = userRepository.findAll(specs, pageable);
        return users.stream().map(this::mapFromUserToResponse).collect(Collectors.toList());
    }

    public UserDto getSingleUser(Long userId) {
        User user = getUserById(userId);
        return mapFromUserToResponse(user);
    }

    public void updateUserLocation(LocationDto request, String username) {
        User user = getUserByEmail(username);
        if (request.getLatitude() != null && request.getLongitude() != null) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            double diameterInMeters = Double.parseDouble(request.getDiameterInMeters());
            user.setLocation(locationService.userLocationToCircle(latitude, longitude, diameterInMeters));
            userRepository.save(user);
        }
    }

    public void updateUserInfo(UserDto request, String username) {
        User user = getUserByEmail(username);

        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDescription(request.getDescription());

        if (user.getIndividualInformation() != null) {
            user.getIndividualInformation().setLastName(request.getLastName());
            if (request.getDateOfBirth() != null) {
                user.getIndividualInformation().setDateOfBirth(Date.valueOf(request.getDateOfBirth()));
            }
        }
        else if (user.getOrganizationInformation() != null) {
            user.getOrganizationInformation().setContactEmail(request.getContactEmail());
            user.getOrganizationInformation().setWebsiteUrl(request.getWebsiteUrl());
            user.getOrganizationInformation().setFacebookPageUrl(request.getFacebookPageUrl());
            user.getOrganizationInformation().setOrganizationNeeds(request.getOrganizationNeeds());
        }
        userRepository.save(user);
    }

    //TODO public void changePassword(String username)

    public void deleteAccount(String username) {
        User user = getUserByEmail(username);
        userRepository.delete(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new IllegalStateException(String.format("user with email %s not found", email)));
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


    //TODO DELETE THIS IF NO ERROR
    //was used on Connection service deleteConnection()
    //and UserService getSingleUser
    //weird
    /*
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("User does not exits"));
    }
    */


    private UserDto mapFromUserToResponse(User user) {
        UserDto response = new UserDto();
        response.setId(user.getId().toString());
        response.setName(user.getName());
        response.setAccountType(user.getUserRole().name());
        response.setEmail(user.getEmail());
        Image profileImage = imageService.getProfileImage(user);
        response.setProfileImage(imageService.createFileDownloadUri(profileImage));
        response.setPhoneNumber(user.getPhoneNumber());
        response.setDescription(user.getDescription());
        response.setCommunityStanding(String.valueOf(user.getCommunityStanding()));

        if (user.getIndividualInformation() != null) {
            response.setLastName(user.getIndividualInformation().getLastName());
            if (user.getIndividualInformation().getDateOfBirth() != null) {
                response.setDateOfBirth(user.getIndividualInformation().getDateOfBirth().toString());
            }
        }
        else if (user.getOrganizationInformation() != null) {
            response.setContactEmail(user.getOrganizationInformation().getContactEmail());
            response.setWebsiteUrl(user.getOrganizationInformation().getWebsiteUrl());
            response.setFacebookPageUrl(user.getOrganizationInformation().getFacebookPageUrl());
            response.setOrganizationNeeds(user.getOrganizationInformation().getOrganizationNeeds());
        }
        return response;
    }
}
