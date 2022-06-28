package app.rescue.backend.service;

import app.rescue.backend.model.ConfirmationToken;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.AuthenticationDto;
import app.rescue.backend.payload.LoginDto;
import app.rescue.backend.payload.RegistrationDto;
import app.rescue.backend.security.JwtProvider;
import app.rescue.backend.utility.EmailSender;
import app.rescue.backend.utility.EmailValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService underTest;

    @Mock
    private UserService userService;

    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @Mock
    private ConnectionService connectionService;
    @Mock
    private LocationService locationService;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private EmailValidator emailValidator;
    @Mock
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        underTest = new AuthService(userService, confirmationTokenService, connectionService,
                locationService, authenticationManager, jwtProvider, emailValidator, emailSender);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canRegister() throws MessagingException {
        // given
        RegistrationDto request = getRegistrationDto();
        String userRole = "INDIVIDUAL";

        given(emailValidator.isValidEmail(request.getEmail())).willReturn(true);

        // when
        underTest.register(request, userRole);

        // then
        verify(userService).signUpUser(any());
        verify(emailSender).send(anyString(), anyString(), anyString());
    }

    @Test
    void registerWillThrowWhenEmailNotValid() throws MessagingException {
        // given
        RegistrationDto request = getRegistrationDto();
        String userRole = "INDIVIDUAL";

        given(emailValidator.isValidEmail(request.getEmail())).willReturn(false);

        // when
        // then
        assertThatThrownBy(() -> underTest.register(request, userRole))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email not valid");

        verify(userService, never()).signUpUser(any());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }



    @Test
    void invitationRegistration() {
        // given
        User newUser = mock(User.class);
        String referralToken = UUID.randomUUID().toString();
        User user = mock(User.class);

        given(userService.findByReferralToken(referralToken)).willReturn(user);

        // when
        underTest.invitationRegistration(newUser, referralToken);

        // then
        verify(connectionService).invitationRegistration(newUser, user);
    }

    @Test
    void canLogin() {
        // given
        LoginDto expected = getLoginDto();
        User user = mock(User.class);

        given(userService.isUserEnabled(expected.getEmail())).willReturn(true);
        given(userService.getUserByEmail(expected.getEmail())).willReturn(user);
        given(user.getUserRole()).willReturn(Role.INDIVIDUAL);

        // when
        AuthenticationDto actual = underTest.login(expected);

        // then
        verify(authenticationManager).authenticate(any());
        verify(jwtProvider).generateToken(any());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    @Test
    void loginWillThrowWhenUserNotEnabled() {
        // given
        LoginDto request = getLoginDto();
        given(userService.isUserEnabled(request.getEmail())).willReturn(false);

        // when
        // then
        assertThatThrownBy(() -> underTest.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is not enabled, check your emails");
    }

    @Test
    void canConfirmToken() {
        // given
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        User user = mock(User.class);

        given(confirmationTokenService.getToken(token)).willReturn(confirmationToken);
        given(confirmationToken.getConfirmedAt()).willReturn(null);
        given(confirmationToken.getExpiresAt()).willReturn(LocalDateTime.now().plusMinutes(10));
        given(confirmationToken.getUser()).willReturn(user);

        // when
        underTest.confirmToken(token);

        // then
        verify(confirmationTokenService).setConfirmedAt(token);
        verify(userService).enableUser(user.getEmail());
        verify(connectionService).completeRefConnection(user);
    }

    @Test
    void confirmTokenWillThrowWhenEmailAlreadyConfirmed() {
        // given
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        given(confirmationTokenService.getToken(token)).willReturn(confirmationToken);
        given(confirmationToken.getConfirmedAt()).willReturn(LocalDateTime.now());

        // when
        // then
        assertThatThrownBy(() -> underTest.confirmToken(token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email already confirmed");
    }

    @Test
    void confirmTokenWillThrowWhenTokenIsExpired() {
        // given
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        given(confirmationTokenService.getToken(token)).willReturn(confirmationToken);
        given(confirmationToken.getConfirmedAt()).willReturn(null);
        given(confirmationToken.getExpiresAt()).willReturn(LocalDateTime.now().minusMinutes(10));

        // when
        // then
        assertThatThrownBy(() -> underTest.confirmToken(token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("token expired");
    }

    private RegistrationDto getRegistrationDto() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("email");
        registrationDto.setName("name");

        return registrationDto;

    }

    private LoginDto getLoginDto() {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("email");

        return loginDto;
    }
}