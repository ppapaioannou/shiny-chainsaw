package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.repository.NotificationRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private NotificationService underTest;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private ConnectionService connectionService;

    @BeforeEach
    void setUp() {
        underTest = new NotificationService(notificationRepository, userService, connectionService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canSendPendingConnectionRequestNotification() {
        // given
        User user = mock(User.class);
        User connectedToUser = mock(User.class);
        Connection connection = getConnection(user, connectedToUser, "PENDING");

        given(user.getId()).willReturn(1L);

        // when
        underTest.sendConnectionRequestNotification(connection);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertEquals(user.getId(), capturedNotification.getSenderId());
        assertEquals("New Friend Request", capturedNotification.getText());
        assertEquals("CONNECTION-REQUEST", capturedNotification.getNotificationType());
    }

    @Test
    void canSendFollowerConnectionRequestNotification() {
        // given
        User user = mock(User.class);
        User organization = mock(User.class);
        Connection connection = getConnection(user, organization, "FOLLOWER");

        given(user.getId()).willReturn(1L);

        // when
        underTest.sendConnectionRequestNotification(connection);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertEquals(user.getId(), capturedNotification.getSenderId());
        assertEquals("New Follower", capturedNotification.getText());
        assertEquals("CONNECTION-FOLLOWER", capturedNotification.getNotificationType());
    }

    @Test
    void canSendConnectionAcceptedNotification() {
        // given
        User user = mock(User.class);
        User organization = mock(User.class);
        Connection connection = getConnection(user, organization, "FOLLOWER");

        given(user.getId()).willReturn(1L);
        given(user.getName()).willReturn("name");

        // when
        underTest.sendConnectionAcceptedNotification(connection);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertEquals(user.getId(), capturedNotification.getSenderId());
        assertEquals(String.format("You are now friends with %s",user.getName()), capturedNotification.getText());
        assertEquals("CONNECTION-ACCEPT", capturedNotification.getNotificationType());
    }

    @Test
    void canSendInvitationCompletedNotification() {
        // given
        User newUser = mock(User.class);

        given(newUser.getId()).willReturn(1L);

        // when
        underTest.sendInvitationCompletedNotification(newUser);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertEquals(newUser.getId(), capturedNotification.getSenderId());
        assertEquals("Your friend created a Rescue account, when they enable their " +
                "account you two will be automatically connected", capturedNotification.getText());
        assertEquals("REGISTRATION-INVITATION", capturedNotification.getNotificationType());
    }

    @Test
    void canSendNewPostNotificationToConnections() {
        // given
        User postOwner = mock(User.class);
        User connectedToUser = mock(User.class);
        Post post = mock(Post.class);

        List<Connection> userConnections = new ArrayList<>();
        Connection connection = mock(Connection.class);

        userConnections.add(connection);

        given(post.getUser()).willReturn(postOwner);
        given(post.getPostType()).willReturn("test");
        given(connectionService.findConnectionsByUser(postOwner)).willReturn(userConnections);
        given(connection.getUser()).willReturn(connectedToUser);
        given(userService.existsById(connection.getConnectedToId())).willReturn(true);

        // when
        underTest.sendNewPostNotification(post);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertEquals("There is a new " + post.getPostType().toUpperCase() + " post from your connections", capturedNotification.getText());
        assertEquals("POST-CONNECTIONS", capturedNotification.getNotificationType());
    }

    @Test
    void canSendNewPostProximityNotification() {
        // given
        User postOwner = mock(User.class);
        User nearUser = mock(User.class);
        Post post = mock(Post.class);

        Geometry nearUserLocation = mock(Geometry.class);
        Geometry postLocation = mock(Geometry.class);

        List<User> users = new ArrayList<>();

        users.add(postOwner);
        users.add(nearUser);

        given(post.getUser()).willReturn(postOwner);
        given(post.getPostType()).willReturn("test");
        given(userService.findAll()).willReturn(users);
        given(nearUser.getLocation()).willReturn(nearUserLocation);
        given(post.getLocation()).willReturn(postLocation);
        given(nearUserLocation.contains(postLocation)).willReturn(true);

        // when
        underTest.sendNewPostNotification(post);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());

        Notification capturedNotification = notificationArgumentCaptor.getValue();

        assertEquals("There is a new " + post.getPostType().toUpperCase() + " post near you", capturedNotification.getText());
        assertEquals("POST-PROXIMITY", capturedNotification.getNotificationType());
    }

    @Test
    void canSendNewCommentNotification() {
        // given
        User postOwner = mock(User.class);
        User commentator = mock(User.class);
        Post post = mock(Post.class);

        List<User> postCommentators = new ArrayList<>();
        User otherCommentator = mock(User.class);
        postCommentators.add(otherCommentator);

        Comment comment = mock(Comment.class);

        given(postOwner.getId()).willReturn(1L);
        given(commentator.getId()).willReturn(2L);
        given(otherCommentator.getId()).willReturn(3L);
        given(comment.getUser()).willReturn(commentator);
        given(comment.getPost()).willReturn(post);
        given(post.getUser()).willReturn(postOwner);

        given(post.getCommentators()).willReturn(postCommentators);


        // when
        underTest.sendNewCommentNotification(comment);

        // then
        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository, times(2)).save(notificationArgumentCaptor.capture());

        List<Notification> capturedNotification = notificationArgumentCaptor.getAllValues();

        assertEquals(postOwner.getId(), capturedNotification.get(0).getUser().getId());
        assertEquals(commentator.getId(), capturedNotification.get(0).getSenderId());
        assertEquals("There is a new comment on your post", capturedNotification.get(0).getText());
        assertEquals("COMMENT-OWNER", capturedNotification.get(0).getNotificationType());

        assertEquals(otherCommentator.getId(), capturedNotification.get(1).getUser().getId());
        assertEquals(commentator.getId(), capturedNotification.get(1).getSenderId());
        assertEquals("There is a new comment on a post you also commented", capturedNotification.get(1).getText());
        assertEquals("COMMENT-COMMENTATOR", capturedNotification.get(1).getNotificationType());
    }

    @Test
    void canGetAllNotifications() {
        // given
        String username = "user@example.com";

        // when
        underTest.getAllNotifications(username);

        // then
        verify(notificationRepository).findAllByUserOrderByIdDesc(any());
    }

    @Test
    void canGetNumberOfUnreadNotifications() {
        // given
        String username = "user@example.com";

        // when
        underTest.getNumberOfUnreadNotifications(username);

        // then
        verify(notificationRepository).getUnreadNotification(any());
    }

    @Test
    void canReadNotification() {
        // given
        Long notificationId = 1L;
        String username = "user@example.com";

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
        given(notification.getUser()).willReturn(user);
        given(user.getEmail()).willReturn(username);

        // when
        underTest.readNotification(notificationId, username);
        // then
        verify(notificationRepository).notificationRead(any(), any());
    }

    @Test
    void readNotificationWillThrowWhenNotNotificationOwner() {
        // given
        Long notificationId = 1L;
        String username = "user@example.com";

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
        given(notification.getUser()).willReturn(user);
        given(user.getEmail()).willReturn("other-user@example.com");

        // when
        // then
        assertThatThrownBy(() -> underTest.readNotification(notificationId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not the owner of this notification");
    }

    @Test
    void canDeleteNotification() {
        // given
        Long notificationId = 1L;
        String username = "user@example.com";

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
        given(notification.getUser()).willReturn(user);
        given(user.getEmail()).willReturn(username);

        // when
        underTest.deleteNotification(notificationId, username);
        // then
        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotificationWillThrowWhenNotNotificationOwner() {
        // given
        Long notificationId = 1L;
        String username = "user@example.com";

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
        given(notification.getUser()).willReturn(user);
        given(user.getEmail()).willReturn("other-user@example.com");

        // when
        // then
        assertThatThrownBy(() -> underTest.deleteNotification(notificationId, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not the owner of this notification");
    }

    private Connection getConnection(User user, User connectedToUser, String connectionStatus) {
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setConnectedToId(connectedToUser.getId());
        connection.setConnectionStatus(connectionStatus);
        return connection;
    }
}