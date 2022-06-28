package app.rescue.backend.service;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.repository.ConnectionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    private ConnectionService underTest;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        underTest = new ConnectionService(connectionRepository, userService, imageService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canConnectWithIndividual() {
        // given
        Long userId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(userId)).willReturn(connectedToUser);

        given(user.getUserRole()).willReturn(Role.INDIVIDUAL);
        given(connectedToUser.getUserRole()).willReturn(Role.INDIVIDUAL);


        // when
        underTest.connectWith(userId, username);

        // then
        ArgumentCaptor<Connection> connectionArgumentCaptor = ArgumentCaptor.forClass(Connection.class);

        verify(connectionRepository).save(connectionArgumentCaptor.capture());

        Connection capturedConnection = connectionArgumentCaptor.getValue();


        assertEquals(user, capturedConnection.getUser());
        assertEquals(userId, capturedConnection.getConnectedToId());
        assertEquals("PENDING", capturedConnection.getConnectionStatus());
    }

    @Test
    void canConnectWithOrganization() {
        // given
        Long userId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(userId)).willReturn(connectedToUser);

        given(user.getUserRole()).willReturn(Role.INDIVIDUAL);
        given(connectedToUser.getUserRole()).willReturn(Role.ORGANIZATION);


        // when
        underTest.connectWith(userId, username);

        // then
        ArgumentCaptor<Connection> connectionArgumentCaptor = ArgumentCaptor.forClass(Connection.class);

        verify(connectionRepository).save(connectionArgumentCaptor.capture());

        Connection capturedConnection = connectionArgumentCaptor.getValue();


        assertEquals(user, capturedConnection.getUser());
        assertEquals(userId, capturedConnection.getConnectedToId());
        assertEquals("FOLLOWER", capturedConnection.getConnectionStatus());
    }

    @Test
    void connectWithWillThrowWhenUsersAreSame() {
        // given
        Long userId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(userId)).willReturn(user);

        // when
        // then
        assertThatThrownBy(() -> underTest.connectWith(userId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("You cannot connect with yourself");
    }

    @Test
    void connectWithWillThrowWhenUserIsOrganization() {
        // given
        Long userId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(userId)).willReturn(connectedToUser);

        given(user.getUserRole()).willReturn(Role.ORGANIZATION);

        // when
        // then
        assertThatThrownBy(() -> underTest.connectWith(userId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organizations can't make connection requests");
    }

    @Test
    void connectWithWillThrowWhenUsersAlreadyConnected() {
        // given
        Long userId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);
        Connection connection = mock(Connection.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(userId)).willReturn(connectedToUser);
        given(connectedToUser.getId()).willReturn(userId);

        given(user.getUserRole()).willReturn(Role.INDIVIDUAL);
        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToUser.getId())).willReturn(Optional.of(connection));

        // when
        // then
        assertThatThrownBy(() -> underTest.connectWith(userId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Users are connected or there is already a connection request in progress");
    }

    @Test
    void canGetAllFriendRequestsConnections() {
        // given
        String connectionType = "friend-requests";
        String username = "user@example.com";

        User user = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);

        // when
        underTest.getAllConnections(connectionType, username);

        // then
        verify(connectionRepository).getAllByConnectedToIdAndConnectionStatus(any(), any());
    }

    @Test
    void canGetAllFollowersConnections() {
        // given
        String connectionType = "followers";
        String username = "user@example.com";

        User user = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);

        // when
        underTest.getAllConnections(connectionType, username);

        // then
        verify(connectionRepository).getAllByConnectedToIdAndConnectionStatus(any(), any());
    }

    @Test
    void canGetAllFriendsConnections() {
        // given
        String connectionType = "friends";
        String username = "user@example.com";

        User user = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);

        // when
        underTest.getAllConnections(connectionType, username);

        // then
        verify(connectionRepository).getAllByUserAndConnectionStatus(any(), any());
    }

    @Test
    void canGetAllOrganizationsConnections() {
        // given
        String connectionType = "organizations";
        String username = "user@example.com";

        User user = mock(User.class);

        given(userService.getUserByEmail(username)).willReturn(user);

        // when
        underTest.getAllConnections(connectionType, username);

        // then
        verify(connectionRepository).getAllByUserAndConnectionStatus(any(), any());
    }

    @Test
    void getAllConnectionsWillThrowWhenUnknownConnectionType() {
        // given
        String connectionType = "random";
        String username = "user@example.com";

        // when
        // then
        assertThatThrownBy(() -> underTest.getAllConnections(connectionType, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not a connection type");
    }

    @Test
    void canGetConnectionStatus() {
        // given
        Long connectedToId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);
        Connection connection = mock(Connection.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId)).willReturn(Optional.of(connection));
        given(connection.getConnectionStatus()).willReturn("status");

        // when
        String actual = underTest.getConnectionStatus(connectedToId, username);

        // then
        assertThat(actual).isEqualTo("status");
    }

    @Test
    void canAcceptConnection() {
        // given
        Long connectedToId = 0L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        Connection connection = mock(Connection.class);

        given(user.getId()).willReturn(1L);
        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(connectedToId)).willReturn(connectedToUser);
        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId)).willReturn(Optional.empty());
        given(connectionRepository.findPendingConnection(connectedToUser, user.getId())).willReturn(Optional.of(connection));
        given(user.getUserRole()).willReturn(Role.INDIVIDUAL);

        // when
        underTest.acceptConnection(connectedToId, username);

        // then
        verify(connectionRepository).save(any());
        verify(connectionRepository).completeConnection(connectedToUser, user.getId());
    }

    @Test
    void acceptConnectionWillThrowWhenUserAlreadyConnected() {
        // given
        Long connectedToId = 0L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        Connection connection = mock(Connection.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(connectedToId)).willReturn(connectedToUser);
        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId))
                .willReturn(Optional.of(connection));

        // when
        // then
        assertThatThrownBy(() -> underTest.acceptConnection(connectedToId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Users already connected");
    }

    @Test
    void acceptConnectionWillThrowWhenNoPendingConnection() {
        // given
        Long connectedToId = 0L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        given(user.getId()).willReturn(1L);
        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(connectedToId)).willReturn(connectedToUser);
        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.acceptConnection(connectedToId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("No connection request found from user ID:%s",connectedToId));
    }

    @Test
    void acceptConnectionWillThrowWhenUserIsOrganization() {
        // given
        Long connectedToId = 0L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        Connection connection = mock(Connection.class);

        given(user.getId()).willReturn(1L);
        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(connectedToId)).willReturn(connectedToUser);
        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId)).willReturn(Optional.empty());
        given(connectionRepository.findPendingConnection(connectedToUser, user.getId())).willReturn(Optional.of(connection));
        given(user.getUserRole()).willReturn(Role.ORGANIZATION);
        given(connectedToUser.getUserRole()).willReturn(Role.INDIVIDUAL);

        // when
        // then
        assertThatThrownBy(() -> underTest.acceptConnection(connectedToId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This user can not accept connection requests");
    }

    @Test
    void canDeclineConnection() {
        // given
        Long connectedToId = 0L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        Connection connection = mock(Connection.class);

        given(user.getId()).willReturn(1L);
        given(userService.getUserByEmail(username)).willReturn(user);
        given(userService.getUserById(connectedToId)).willReturn(connectedToUser);
        given(connectionRepository.findConnectionByUserAndConnectedToId(connectedToUser, user.getId()))
                .willReturn(Optional.of(connection));

        // when
        underTest.declineConnection(connectedToId, username);

        // then
        verify(connectionRepository).delete(any());
    }



    @Test
    void invitationFromIndividualRegistration() {
        // given
        User newUser = mock(User.class);
        User invitedByUser = mock(User.class);

        given(newUser.getUserRole()).willReturn(Role.INDIVIDUAL);
        given(invitedByUser.getUserRole()).willReturn(Role.INDIVIDUAL);

        // when
        underTest.invitationRegistration(newUser, invitedByUser);

        // then
        verify(connectionRepository, times(2)).save(any());
    }

    @Test
    void invitationFromOrganizationRegistration() {
        // given
        User newUser = mock(User.class);
        User invitedByUser = mock(User.class);

        given(newUser.getUserRole()).willReturn(Role.INDIVIDUAL);
        given(invitedByUser.getUserRole()).willReturn(Role.ORGANIZATION);

        // when
        underTest.invitationRegistration(newUser, invitedByUser);

        // then
        verify(connectionRepository).save(any());
    }

    @Test
    void canCompleteRefIndividualConnection() {
        // given
        User user = mock(User.class);
        User connectedToUser = mock(User.class);
        Connection connection = mock(Connection.class);

        given(connectionRepository.findRefPendingConnection((user))).willReturn(Optional.of(connection));
        given(connectedToUser.getUserRole()).willReturn(Role.INDIVIDUAL);
        given(userService.getUserById(connection.getConnectedToId())).willReturn(connectedToUser);

        // when
        underTest.completeRefConnection(user);

        // then
        verify(connectionRepository, times(2)).completeConnection(any(), any());

    }

    @Test
    void canCompleteRefOrganizationConnection() {
        // given
        User user = mock(User.class);
        User connectedToUser = mock(User.class);
        Connection connection = mock(Connection.class);

        given(connectionRepository.findRefPendingConnection((user))).willReturn(Optional.of(connection));
        given(connectedToUser.getUserRole()).willReturn(Role.ORGANIZATION);
        given(userService.getUserById(connection.getConnectedToId())).willReturn(connectedToUser);

        // when
        underTest.completeRefConnection(user);

        // then
        verify(connectionRepository).completeRefOrgConnection(any());
    }

    @Test
    void canDeleteConnection() {
        // given
        Long connectedToId = 1L;
        String username = "user@example.com";

        User user = mock(User.class);
        User connectedToUser = mock(User.class);

        Connection connection1 = mock(Connection.class);
        Connection connection2 = mock(Connection.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(user.getId()).willReturn(2L);
        given(userService.getUserById(connectedToId)).willReturn(connectedToUser);

        given(connectionRepository.findConnectionByUserAndConnectedToId(user, connectedToId)).willReturn(Optional.of(connection1));
        given(connectionRepository.findConnectionByUserAndConnectedToId(connectedToUser, user.getId())).willReturn(Optional.of(connection2));

        // when
        underTest.deleteConnection(connectedToId, username);
        // then
        verify(connectionRepository, times(2)).delete(any());
    }

    @Test
    void canFindConnectionsByUser() {
        // given
        User user = mock(User.class);

        // when
        underTest.findConnectionsByUser(user);

        // then
        verify(connectionRepository).findConnectionsByUser(user);
    }
}