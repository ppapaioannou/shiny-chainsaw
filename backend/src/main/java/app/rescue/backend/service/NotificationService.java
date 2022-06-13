package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.NotificationDto;
import app.rescue.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    private final UserService userService;
    private final ConnectionService connectionService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService,
                               ConnectionService connectionService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.connectionService = connectionService;
    }

    public void sendConnectionRequestNotification(Connection connection) {
        Notification connectionNotification = new Notification();
        connectionNotification.setUser(userService.getUserById(connection.getConnectedToId()));
        connectionNotification.setSenderId(connection.getUser().getId());
        if (connection.getConnectionStatus().equals("PENDING")) {
            connectionNotification.setText("New Friend Request");
            connectionNotification.setNotificationType("CONNECTION-REQUEST");
        }
        else if (connection.getConnectionStatus().equals("FOLLOWER")) {
            connectionNotification.setText("New Follower");
            connectionNotification.setNotificationType("CONNECTION-FOLLOWER");
        }
        notificationRepository.save(connectionNotification);
    }

    public void sendConnectionAcceptedNotification(Connection connection) {
        Notification connectionAcceptedNotification = new Notification();
        connectionAcceptedNotification.setUser(userService.getUserById(connection.getConnectedToId()));
        connectionAcceptedNotification.setSenderId(connection.getUser().getId());
        connectionAcceptedNotification.setText(String.format("You are now friends with %s",connection.getUser().getName()));
        connectionAcceptedNotification.setNotificationType("CONNECTION-ACCEPT");

        notificationRepository.save(connectionAcceptedNotification);
    }

    public void sendInvitationCompletedNotification(User newUser) {
        Notification connectionAcceptedNotification = new Notification();
        User invitedByUser = userService.getUserById(newUser.getInvitedByUserId());
        connectionAcceptedNotification.setUser(invitedByUser);
        connectionAcceptedNotification.setSenderId(newUser.getId());
        connectionAcceptedNotification.setText("Your friend created a Rescue account, " +
                "when they enable their account you two will be automatically connected");
        connectionAcceptedNotification.setNotificationType("REGISTRATION-INVITATION");
        connectionAcceptedNotification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(connectionAcceptedNotification);
    }

    public void sendNewPostNotification(Post post) {
        Collection<User> notified = sendConnectionNotifications(post);
        sendProximityNotifications(post, notified);
    }

    public void sendNewCommentNotification(Comment comment) {
        if (!comment.getUser().equals(comment.getPost().getUser())) {
            Notification newCommentNotification = new Notification();
            newCommentNotification.setUser(comment.getPost().getUser());
            newCommentNotification.setSenderId(comment.getUser().getId());
            newCommentNotification.setPost(comment.getPost());
            newCommentNotification.setText("There is a new comment on your post");
            newCommentNotification.setNotificationType("COMMENT-OWNER");

            notificationRepository.save(newCommentNotification);
        }
        sendAdditionalCommentNotifications(comment);
    }

    public List<NotificationDto> getAllNotifications(String username) {
        User user = userService.getUserByEmail(username);
        List<Notification> notifications = notificationRepository.findAllByUserOrderByIdDesc(user);
        return notifications.stream().map(this::mapFromNotificationToResponse).collect(Collectors.toList());
    }

    public int getNumberOfUnreadNotifications(String username) {
        User user = userService.getUserByEmail(username);
        Optional<List<Notification>> notifications = notificationRepository.getUnreadNotification(user);
        return notifications.map(List::size).orElse(0);

    }

    public void readNotification(Long notificationId, String username) {
        Notification notification = getNotificationById(notificationId);
        if (notification.getUser().getEmail().equals(username)) {
            notificationRepository.notificationRead(notification, LocalDateTime.now());
        }
        else {
            throw new IllegalStateException("Not the owner of this notification");
        }
    }

    public void deleteNotification(Long notificationId, String username) {
        Notification notification = getNotificationById(notificationId);
        if (notification.getUser().getEmail().equals(username)) {
            notificationRepository.delete(notification);
        }
        else {
            throw new IllegalStateException("Not the owner of this notification");
        }
    }

    private Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(String.format("Notification with ID:%s does not exist", id)));
    }

    private Collection<User> sendConnectionNotifications(Post post) {
        Collection<User> notified = new HashSet<>();
        for (Connection connection : connectionService.findConnectionsByUser(post.getUser())) {
            if (userService.existsById(connection.getConnectedToId())) {
                Notification newPostNotification = new Notification();
                newPostNotification.setUser(userService.getUserById(connection.getConnectedToId()));
                newPostNotification.setSenderId(connection.getUser().getId());
                newPostNotification.setPost(post);

                String postType = post.getPostType();
                String notificationText = "There is a new " + postType.toUpperCase() + " post from your connections";

                newPostNotification.setText(notificationText);
                newPostNotification.setNotificationType("POST-CONNECTIONS");

                notificationRepository.save(newPostNotification);

                notified.add(newPostNotification.getUser());
            }
        }
        return notified;
    }

    private void sendProximityNotifications(Post post, Collection<User> notified) {
        for (User user : userService.findAll()) {
            if ((!notified.contains(user)) && (!user.equals(post.getUser())) &&
                    (user.getLocation() != null) && (post.getLocation() != null)) {
                if (user.getLocation().contains(post.getLocation())) {
                    Notification newPostNotification = new Notification();
                    newPostNotification.setUser(user);
                    newPostNotification.setSenderId(post.getUser().getId());
                    newPostNotification.setPost(post);

                    String postType = post.getPostType();
                    String notificationText = "There is a new " + postType.toUpperCase() + " post near you";

                    newPostNotification.setText(notificationText);
                    newPostNotification.setNotificationType("POST-PROXIMITY");

                    notificationRepository.save(newPostNotification);
                }
            }
        }
    }

    private void sendAdditionalCommentNotifications(Comment comment) {
        for (User commentator : comment.getPost().getCommentators()) {
            if (!commentator.equals(comment.getUser())) {
                Notification newCommentNotification = new Notification();
                newCommentNotification.setUser(commentator);
                newCommentNotification.setSenderId(comment.getUser().getId());
                newCommentNotification.setPost(comment.getPost());
                newCommentNotification.setText("There is a new comment on a post you also commented");
                newCommentNotification.setNotificationType("COMMENT-COMMENTATOR");

                notificationRepository.save(newCommentNotification);
            }
        }
    }

    private NotificationDto mapFromNotificationToResponse(Notification notification) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

        NotificationDto notificationResponse = new NotificationDto();
        notificationResponse.setId(notification.getId());
        notificationResponse.setSender(userService.getUserById(notification.getSenderId()).getName());
        if (notification.getPost() != null) {
            Post post = notification.getPost();
            notificationResponse.setPost(post.getTitle());
            notificationResponse.setPostId(post.getId());
        }
        notificationResponse.setText(notification.getText());
        notificationResponse.setCreatedAt(notification.getCreatedAt().format(dateTimeFormatter));
        if (notification.getReadAt() != null) {
            notificationResponse.setReadAt(notification.getReadAt().format(dateTimeFormatter));
        }


        return notificationResponse;
    }
}
