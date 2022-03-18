package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.ImageRepository;
import app.rescue.backend.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "user with email %s not found";

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    //private final ConnectionService connectionService;
    private final ConnectionRepository connectionRepository;

    public UserService(UserRepository userRepository, ImageRepository imageRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       ConfirmationTokenService confirmationTokenService, ConnectionRepository connectionRepository) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.connectionRepository = connectionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(User user, String referralToken) {
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();

        if (userExists) {
            // TODO check if attributes are the same and if email not confirmed send confirmation email.
            throw new IllegalStateException("email already taken");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);

        if (referralToken != null) {
            User invitedByUser = userRepository.findByReferralToken(referralToken);
            user.setInvitedBy(invitedByUser);
            userRepository.save(user);

            //connectionService.connect(user, invitedByUser);
            if (user.getUserRole() == Role.INDIVIDUAL && invitedByUser.getUserRole() == Role.INDIVIDUAL) {
                connectionRepository.save(new Connection(user, invitedByUser, "CONNECTED"));
                connectionRepository.save(new Connection(invitedByUser, user, "CONNECTED"));
            }
            //TODO this needs fixin'

        }
        else {
            userRepository.save(user);
        }



        //TODO create ImageService class to handle all image operations
        Image profileImage = user.getProfileImage();
        profileImage.setUser(user);
        imageRepository.save(profileImage);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;

    }

    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }

    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return currentUserName;
        }
        else {
            throw new IllegalStateException("No user logged in");
        }
    }


}
