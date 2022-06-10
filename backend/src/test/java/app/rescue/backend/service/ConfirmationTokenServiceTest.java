package app.rescue.backend.service;

import app.rescue.backend.model.ConfirmationToken;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceTest {

    private ConfirmationTokenService underTest;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @BeforeEach
    void setUp() {
        underTest = new ConfirmationTokenService(confirmationTokenRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canSaveConfirmationToken() {
        // given
        User user = getUser();
        ConfirmationToken confirmationToken = new ConfirmationToken(user);

        // when
        underTest.saveConfirmationToken(confirmationToken);

        // then
        ArgumentCaptor<ConfirmationToken> tokenArgumentCaptor = ArgumentCaptor.forClass(ConfirmationToken.class);

        verify(confirmationTokenRepository).save(tokenArgumentCaptor.capture());

        ConfirmationToken capturedToken = tokenArgumentCaptor.getValue();

        assertThat(capturedToken).isEqualTo(confirmationToken);
    }

    @Test
    void canGetToken() {
        // given
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = mock(ConfirmationToken.class);
        given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));

        // when
        underTest.getToken(token);

        // then
        verify(confirmationTokenRepository).findByToken(any());

    }

    @Test
    void getTokenWillThrowWhenTokenNotFound() {
        // given
        String token = UUID.randomUUID().toString();

        // when
        // then
        assertThatThrownBy(() -> underTest.getToken(token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("token not found");

    }

    @Test
    void setConfirmedAt() {
        // given
        String token = UUID.randomUUID().toString();

        // when
        underTest.setConfirmedAt(token);

        // then
        verify(confirmationTokenRepository).updateConfirmedAt(any(), any());
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.INDIVIDUAL);
        return user;
    }
}