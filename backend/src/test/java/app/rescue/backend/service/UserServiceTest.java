package app.rescue.backend.service;

import app.rescue.backend.model.IndividualInformation;
import app.rescue.backend.model.OrganizationInformation;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.repository.IndividualInformationRepository;
import app.rescue.backend.repository.OrganizationInformationRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.utility.AppConstants;
import app.rescue.backend.utility.EmailSender;
import app.rescue.backend.utility.EmailValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService underTest;

    @Mock
    private UserRepository userRepository;
    @Mock
    private IndividualInformationRepository individualInformationRepository;
    @Mock
    private OrganizationInformationRepository organizationInformationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @Mock
    private LocationService locationService;
    @Mock
    private ImageService imageService;

    @Mock
    private EmailSender emailSender;
    @Mock
    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        underTest = new UserService(userRepository, individualInformationRepository, organizationInformationRepository,
                passwordEncoder, confirmationTokenService, locationService, imageService, emailSender, emailValidator);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canSignUpIndividual() {
        // given
        User user = getUser("user@example.com", Role.INDIVIDUAL);

        // when
        underTest.signUpUser(user);

        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userArgumentCaptor.capture());

        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser).isEqualTo(user);
    }

    @Test
    void canSignUpOrganization() {
        // given
        User user = getUser("user@example.com", Role.ORGANIZATION);

        // when
        underTest.signUpUser(user);

        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userArgumentCaptor.capture());

        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser).isEqualTo(user);
    }

    @Test
    void signUpUserWillTrowWhenEmailIsTaken() {
        // given
        User user = getUser("user@example.com", Role.INDIVIDUAL);
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        given(userRepository.existsByEmailAndEnabled(anyString(), anyBoolean())).willReturn(true);

        // when
        // then
        assertThatThrownBy(() -> underTest.signUpUser(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with that email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void signUpUserWillDeleteUserWhenNotEnabled() {
        // given
        User user = getUser("user@example.com", Role.INDIVIDUAL);
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        given(userRepository.existsByEmailAndEnabled(anyString(), anyBoolean())).willReturn(false);

        // when
        underTest.signUpUser(user);
        // then
        verify(userRepository).delete(any());
    }

    @Test
    void canEnableUser() {
        // given
        String userEmail = anyString();

        // when
        underTest.enableUser(userEmail);
        // then
        verify(userRepository).enableUser(userEmail);
    }

    @Test
    void canInviteFriend() {
        // given
        String username = "user@example.com";
        String email = "random@email.com";
        User user = getUser(username, Role.INDIVIDUAL);
        String confirmationLink = "http://localhost:4200/register/individual/ref/"+ user.getReferralToken().strip();

        given(emailValidator.check(email)).willReturn(true);
        given(userRepository.findByEmail(username)).willReturn(Optional.of(user));

        // when
        underTest.inviteFriend(email, username);

        // then
        verify(emailSender).send(email, user.getName(), confirmationLink);
    }

    @Test
    void inviteFriendWillTrowWhenEmailIsNotValid() {
        // given
        String username = "user@example.com";
        String email = "random@email.com";
        User user = getUser(username, Role.INDIVIDUAL);
        String confirmationLink = "http://localhost:4200/register/individual/ref/"+ user.getReferralToken().strip();

        given(emailValidator.check(anyString())).willReturn(false);

        // when
        // then
        assertThatThrownBy(() -> underTest.inviteFriend(email, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email not valid");

        verify(emailSender, never()).send(email, user.getName(), confirmationLink);
    }

    @Test
    @SuppressWarnings("unchecked")
    void canGetAllUsers() {
        // given
        Specification<User> specs = Specification.where(null);
        Pageable pageable = AppConstants.createPageableRequest(0, 10, "id", "desc");

        //Suppress the unchecked warning for mock classes with generic parameters
        Page<User> pagedUsers = mock(Page.class);
        given(userRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(pagedUsers);
        // when
        underTest.getAllUsers(specs, pageable);
        // then
        verify(userRepository).findAll(specs, pageable);
    }

    @Test
    void canGetSingleUser() {
        // given
        User user = getUser("user@example.com", Role.INDIVIDUAL);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        underTest.getSingleUser(anyLong());

        // then
        verify(userRepository).findById(anyLong());
    }

    @Test
    void canUpdateUserLocation() {
        // given
        LocationDto request = mock(LocationDto.class);
        User user = mock(User.class);

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

        given(request.getLatitude()).willReturn(String.valueOf(1L));
        given(request.getLongitude()).willReturn(String.valueOf(1L));
        given(request.getDiameterInMeters()).willReturn(String.valueOf(1L));

        // when
        underTest.updateUserLocation(request, user.getEmail());

        // then
        verify(locationService).turnUserLocationToCircle(Double.parseDouble(request.getLatitude()),
                Double.parseDouble(request.getLongitude()), Double.parseDouble(request.getDiameterInMeters()));
        verify(userRepository).save(any());
    }

    @Test
    void canUpdateUserInfoIndividual() {
        // given
        UserDto request = mock(UserDto.class);
        User user = mock(User.class);

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(user.getIndividualInformation()).willReturn(new IndividualInformation());
        given(request.getDateOfBirth()).willReturn("6789-01-23");

        // when
        underTest.updateUserInfo(request, user.getEmail());

        // then
        verify(userRepository).save(any());
    }
    @Test
    void canUpdateUserInfoOrganization() {
        // given
        UserDto request = mock(UserDto.class);
        User user = mock(User.class);

        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
        given(user.getOrganizationInformation()).willReturn(new OrganizationInformation());

        // when
        underTest.updateUserInfo(request, user.getEmail());

        // then
        verify(userRepository).save(any());
    }


    @Test
    void canDeleteAccount() {
        // given
        User user = mock(User.class);
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

        // when
        underTest.deleteAccount(user.getEmail());

        // then
        verify(userRepository).delete(any());
    }

    @Test
    void canGetUserByEmail() {
        // given
        User user = mock(User.class);
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

        // when
        underTest.getUserByEmail(user.getEmail());

        // then
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void getUserByEmailWillThrowWhenEmailNotFound() {
        // given
        User user = mock(User.class);

        // when
        // then
        assertThatThrownBy(() -> underTest.getUserByEmail(user.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("user with email %s not found", user.getEmail()));
    }

    @Test
    void canGetUserById() {
        // given
        User user = mock(User.class);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        underTest.getUserById(user.getId());

        // then
        verify(userRepository).findById(user.getId());
    }

    @Test
    void getUserByIdWillThrowWhenUserDoesNotExist() {
        // given
        User user = mock(User.class);

        // when
        // then
        assertThatThrownBy(() -> underTest.getUserById(user.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("User with ID:%s does not exist", user.getId()));

    }

    @Test
    void tryToFindUserByIdAndSucceed() {
        // given
        User user = mock(User.class);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        Boolean actual = underTest.tryToFindUserById(user.getId());

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void tryToFindUserByIdAndFail() {
        // given
        User user = mock(User.class);

        // when
        Boolean actual = underTest.tryToFindUserById(user.getId());

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void findByReferralToken() {
        // given
        User user = mock(User.class);
        given(userRepository.findByReferralToken(user.getReferralToken())).willReturn(Optional.of(user));

        // when
        underTest.findByReferralToken(user.getReferralToken());

        // then
        verify(userRepository).findByReferralToken(user.getReferralToken());
    }

    @Test
    void findByReferralTokenWillThrowWhenNotAReferralToken() {
        // given
        User user = mock(User.class);

        // when
        // then
        assertThatThrownBy(() -> underTest.findByReferralToken(user.getReferralToken()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not a referral token");
    }

    @Test
    void findAll() {
        // given
        // when
        underTest.findAll();

        // then
        verify(userRepository).findAll();
    }

    private User getUser(String email, Role role) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(role);
        return user;
    }

}