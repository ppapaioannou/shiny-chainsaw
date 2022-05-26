package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.repository.ImageRepository;
import app.rescue.backend.repository.IndividualInformationRepository;
import app.rescue.backend.repository.OrganizationInformationRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.util.EmailSender;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final IndividualInformationRepository individualInformationRepository;
    private final OrganizationInformationRepository organizationInformationRepository;

    private final PasswordEncoder passwordEncoder;

    private final ConfirmationTokenService confirmationTokenService;
    private final LocationService locationService;
    private final ImageRepository imageRepository;

    private final EmailSender emailSender;

    public UserService(UserRepository userRepository, IndividualInformationRepository individualInformationRepository,
                       OrganizationInformationRepository organizationInformationRepository,
                       PasswordEncoder passwordEncoder,
                       ConfirmationTokenService confirmationTokenService, LocationService locationService, ImageRepository imageRepository, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.individualInformationRepository = individualInformationRepository;
        this.organizationInformationRepository = organizationInformationRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.locationService = locationService;
        this.imageRepository = imageRepository;
        this.emailSender = emailSender;
    }

    public String signUpUser(User newUser) {
        Optional<User> userExists = userRepository.findByEmail(newUser.getEmail());
        if (userExists.isPresent()) {
            if (userExists.get().isEnabled()) {
                throw new IllegalStateException("User with that email already exists");
            }
            userRepository.delete(userExists.get());
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

    public void updateUserLocation(LocationDto request, String userName) {
        User user = getUserByEmail(userName);
        Geometry userLocation = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            double diameterInMeters;
            if (request.getDiameterInMeters().equals("inf")) {
                //earth diameter in meters
                diameterInMeters = 12742000.0;
            }
            else {
                diameterInMeters = Double.parseDouble(request.getDiameterInMeters());
            }

            userLocation = locationService.userLocationToCircle(latitude, longitude, diameterInMeters);
        }
        user.setLocation(userLocation);
        userRepository.save(user);

    }

    public void updateUserInfo(UserDto request, String userName) {
        User user = getUserByEmail(userName);

        user.setName(request.getName());
        if (user.getIndividualInformation() != null) {
            user.getIndividualInformation().setLastName(request.getLastName());
            if (!Objects.equals(request.getDateOfBirth(), "")) {
                user.getIndividualInformation().setDateOfBirth(Date.valueOf(request.getDateOfBirth()));
            }
        }
        else if (user.getOrganizationInformation() != null) {
            user.getOrganizationInformation().setContactEmail(request.getContactEmail());
            user.getOrganizationInformation().setWebsiteUrl(request.getWebsiteUrl());
            user.getOrganizationInformation().setFacebookPageUrl(request.getFacebookPageUrl());
            user.getOrganizationInformation().setOrganizationNeeds(request.getOrganizationNeeds());
        }



        user.setPhoneNumber(request.getPhoneNumber());

        user.setDescription(request.getDescription());

        userRepository.save(user);

    }

    public void deleteUserAccount(String userName) {
        User user = getUserByEmail(userName);
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

    public List<UserDto> getAllUsers(int pageNo, int pageSize, String sortBy, String sortDir,
                                     Specification<User> specs, String userName) {

        Sort sort;
        if (sortDir.equalsIgnoreCase("asc")) {
            sort = Sort.by(sortBy).ascending();
        }
        else if (sortDir.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        }
        else {
            throw new IllegalStateException("unknown sorting method");
        }

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<User> users = userRepository.findAll(specs, pageable);

        return users.stream().map(this::mapFromUserToResponse).collect(Collectors.toList());
    }

    public UserDto getSingleUser(Long userId) {
        User user = findById(userId);
        return mapFromUserToResponse(user);
    }

    public void inviteFriend(String email, String userName) {
        User user = getUserByEmail(userName);
        String name = user.getName();
        String link = "http://localhost:4200/register-individual/ref/"+ user.getReferralToken().strip();

        emailSender.send(email, buildEmail(name, link), true);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("User does not exits"));
    }

    private UserDto mapFromUserToResponse(User user) {
        UserDto response = new UserDto();
        response.setId(user.getId().toString());
        response.setName(user.getName());
        response.setAccountType(user.getUserRole().name());
        response.setEmail(user.getEmail());
        //TODO field Image profileImage on User class
        Image profileImage = imageRepository.findByUserAndProfileImage(user, true);
        String profileImageLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/image/")
                .path(String.valueOf(profileImage.getId()))
                .toUriString();
        response.setProfileImage(profileImageLink);
        response.setPhoneNumber(user.getPhoneNumber());
        response.setDescription(user.getDescription());
        response.setCommunityStanding(String.valueOf(user.getCommunityStanding()));

        if (user.getUserRole().equals(Role.INDIVIDUAL)) {
            response.setLastName(user.getIndividualInformation().getLastName());
            if (user.getIndividualInformation().getDateOfBirth() != null) {
                response.setDateOfBirth(user.getIndividualInformation().getDateOfBirth().toString());
            }

        }
        else if (user.getUserRole().equals(Role.ORGANIZATION)) {
            response.setContactEmail(user.getOrganizationInformation().getContactEmail());
            response.setWebsiteUrl(user.getOrganizationInformation().getWebsiteUrl());
            response.setFacebookPageUrl(user.getOrganizationInformation().getFacebookPageUrl());
            response.setOrganizationNeeds(user.getOrganizationInformation().getOrganizationNeeds());
        }
        return response;
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
    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Come and join Rescue</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hello, your friend " + name + " invited you to join Rescue</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Rescue is an online platform dedicated to bla bla  bla. Please click on the below link to register and join the community: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Join Now</a> </p></blockquote>\n <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
