package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.RegistrationDto;
import app.rescue.backend.payload.LoginDto;
import app.rescue.backend.payload.AuthenticationDto;
import app.rescue.backend.security.JwtProvider;
import app.rescue.backend.utility.EmailSender;
import app.rescue.backend.utility.EmailValidator;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserService userService;

    private final ConfirmationTokenService confirmationTokenService;
    private final ConnectionService connectionService;
    private final LocationService locationService;

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    private final EmailValidator emailValidator;
    private final EmailSender emailSender;


    public AuthService(UserService userService, ConfirmationTokenService confirmationTokenService,
                       ConnectionService connectionService, LocationService locationService,
                       AuthenticationManager authenticationManager, JwtProvider jwtProvider,
                       EmailValidator emailValidator, EmailSender emailSender) {
        this.userService = userService;
        this.confirmationTokenService = confirmationTokenService;
        this.connectionService = connectionService;
        this.locationService = locationService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.emailValidator = emailValidator;
        this.emailSender = emailSender;
    }

    public User register(RegistrationDto request, String userRole) throws MessagingException {
        if (!emailValidator.isValidEmail(request.getEmail())) {
            throw new IllegalStateException("email not valid");
        }
        User newUser = mapFromRequestToUser(request, userRole);

        String token = userService.signUpUser(newUser);

        String confirmationLink = "http://localhost:8080/api/v1/auth/confirm?token=" + token;
        emailSender.send(request.getEmail(), request.getName(), confirmationLink);

        return newUser;
    }

    public void invitationRegistration(User newUser, String referralToken) {
        User invitedByUser = userService.findByReferralToken(referralToken);
        newUser.setInvitedByUserId(invitedByUser.getId());
        connectionService.invitationRegistration(newUser, invitedByUser);
    }

    public AuthenticationDto login(LoginDto request) {
        if (!userService.isUserEnabled(request.getEmail())) {
            throw new IllegalStateException("User is not enabled, check your emails");
        }
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String authenticationToken = jwtProvider.generateToken(authenticate);
        User user = userService.getUserByEmail(request.getEmail());
        return new AuthenticationDto(authenticationToken,
                request.getEmail(),
                user.getId(),
                user.getUserRole().toString());
    }

    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token);

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        userService.enableUser(confirmationToken.getUser().getEmail());

        connectionService.completeRefConnection(confirmationToken.getUser());
        return "confirmed";
    }

    private User mapFromRequestToUser(RegistrationDto request, String userRole) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDescription(request.getDescription());
        user.setUserRole(Role.valueOf(userRole));

        if (userRole.equals("INDIVIDUAL")) {
            IndividualInformation individualInformation = new IndividualInformation();
            individualInformation.setLastName(request.getLastName());
            user.setIndividualInformation(individualInformation);
        }
        else if (userRole.equals("ORGANIZATION")) {
            OrganizationInformation organizationInformation = new OrganizationInformation();
            organizationInformation.setContactEmail(request.getContactEmail());

            if (!request.getAddress().equals("")) {
                //only if location is set, otherwise parseDouble trows error
                organizationInformation.setAddress(request.getAddress());
                double latitude = Double.parseDouble(request.getLatitude());
                double longitude = Double.parseDouble(request.getLongitude());
                double diameterInMeters = 12742000;
                Geometry orgLocation = locationService.turnUserLocationToCircle(latitude, longitude, diameterInMeters);
                user.setLocation(orgLocation);
            }

            organizationInformation.setWebsiteUrl(request.getWebsiteUrl());
            organizationInformation.setFacebookPageUrl(request.getFacebookPageUrl());
            organizationInformation.setOrganizationNeeds(request.getOrganizationNeeds());
            user.setOrganizationInformation(organizationInformation);
        }
        else {
            throw new IllegalStateException("unknown role");
        }

        return user;
    }


}

