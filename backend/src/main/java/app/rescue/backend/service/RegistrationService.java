package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.request.RegistrationRequest;
import app.rescue.backend.util.EmailSender;
import app.rescue.backend.util.EmailValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegistrationService {

    private final UserService userService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final ConnectionService connectionService;


    public RegistrationService(UserService userService, EmailValidator emailValidator,
                               ConfirmationTokenService confirmationTokenService,
                               EmailSender emailSender, ConnectionService connectionService) {
        this.userService = userService;
        this.emailValidator = emailValidator;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSender = emailSender;
        this.connectionService = connectionService;
    }

    public User register(RegistrationRequest requestDto, String userRole, String referralToken) {
        boolean isValidEmail = emailValidator.check(requestDto.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }
        User newUser = mapFromRequestToUser(requestDto, userRole);

        String token = userService.signUpUser(newUser, referralToken);

        invitationRegistration(newUser, referralToken);

        String link = "http://localhost:8080/api/v1/registration/confirm?token=" + token;
        emailSender.send(requestDto.getEmail(), buildEmail(requestDto.getName(), link));

        return newUser;
    }

    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token).orElseThrow(() ->
                new IllegalStateException("token not found"));

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

    //TODO create mapper class to handle this conversion
    private User mapFromRequestToUser(RegistrationRequest requestDto, String userRole) {
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());
        user.setName(requestDto.getName());

        user.setProfileImage(requestDto.getProfileImageData());
        user.setPhoneNumber(requestDto.getPhoneNumber());
        user.setDescription(requestDto.getDescription());
        user.setUserRole(Role.valueOf(userRole));

        if (userRole.equals("INDIVIDUAL")) {
            IndividualInformation individualInformation = new IndividualInformation();
            individualInformation.setLastName(requestDto.getLastName());
            //individualInformation.setDateOfBirth(Date.valueOf(requestDto.getDateOfBirth()));
            user.setIndividualInformation(individualInformation);
        }
        else if (userRole.equals("ORGANIZATION")) {
            OrganizationInformation organizationInformation = new OrganizationInformation();
            organizationInformation.setContactEmail(requestDto.getContactEmail());
            organizationInformation.setRegion(requestDto.getRegion());
            organizationInformation.setAddress(requestDto.getAddress());
            organizationInformation.setCity(requestDto.getCity());
            organizationInformation.setZipCode(requestDto.getZipCode());
            organizationInformation.setWebsiteUrl(requestDto.getWebsiteUrl());
            organizationInformation.setFacebookPageUrl(requestDto.getFacebookPageUrl());
            organizationInformation.setOrganizationNeeds(requestDto.getOrganizationNeeds());
            user.setOrganizationInformation(organizationInformation);
        }
        else {
            throw new IllegalStateException("unknown role");
        }

        return user;
    }

    private void invitationRegistration(User user, String referralToken) {
        if (referralToken != null) {
            User invitedByUser = userService.findByReferralToken(referralToken);
            user.setInvitedByUserId(invitedByUser.getId());
            connectionService.invitationRegistration(user, invitedByUser);

        }
    }

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
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
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
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
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

