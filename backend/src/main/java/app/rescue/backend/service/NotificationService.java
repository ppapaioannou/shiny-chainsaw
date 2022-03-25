package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.NotificationRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.util.LocationHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final LocationHelper locationHelper;

    public NotificationService(NotificationRepository notificationRepository, ConnectionRepository connectionRepository,
                               UserRepository userRepository, LocationHelper locationHelper) {
        this.notificationRepository = notificationRepository;
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.locationHelper = locationHelper;
    }

    public void sendConnectionNotification(Connection connection) {
        Notification connectionNotification = new Notification();
        connectionNotification.setUser(connection.getConnectedTo());
        connectionNotification.setSender(connection.getUser());
        if (connection.getConnectionStatus().equals("PENDING")) {
            connectionNotification.setText("New Friend Request");
        }
        else if (connection.getConnectionStatus().equals("FOLLOWER")) {
            connectionNotification.setText("New Follower");
        }
        connectionNotification.setNotificationType("Connection");
        connectionNotification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(connectionNotification);
    }

    public void sendConnectionAcceptedNotification(Connection connection) {
        Notification connectionAcceptedNotification = new Notification();
        connectionAcceptedNotification.setUser(connection.getConnectedTo());
        connectionAcceptedNotification.setSender(connection.getUser());
        connectionAcceptedNotification.setText(String.format("You are now friends with %s",connection.getUser().getName()));
        connectionAcceptedNotification.setNotificationType("Connection");
        connectionAcceptedNotification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(connectionAcceptedNotification);
    }

    public void sendInvitationCompleteNotification(User newUser) {
        Notification connectionAcceptedNotification = new Notification();
        connectionAcceptedNotification.setUser(newUser.getInvitedBy());
        connectionAcceptedNotification.setSender(newUser);
        connectionAcceptedNotification.setText("Your friend created a Rescue account");
        connectionAcceptedNotification.setNotificationType("Registration");
        connectionAcceptedNotification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(connectionAcceptedNotification);
    }

    public void sendNewPostNotification(Post animalPost) {
        Set<User> notified = sendConnectionNotifications(animalPost);
        sendProximityNotifications(animalPost, notified);


    }

    public void sendNewCommentNotification(Comment comment) {
        if (!comment.getUser().equals(comment.getPost().getUser())) {
            Notification newCommentNotification = new Notification();
            newCommentNotification.setUser(comment.getPost().getUser());
            newCommentNotification.setSender(comment.getUser());
            newCommentNotification.setPost(comment.getPost());
            newCommentNotification.setText("There is a new comment on your post");
            newCommentNotification.setNotificationType("Comment");
            newCommentNotification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(newCommentNotification);
        }
        sendAdditionalCommentNotifications(comment);
    }

    private Set<User> sendConnectionNotifications(Post animalPost) {
        Set<User> notified = new HashSet<>();
        for (Connection connection : connectionRepository.findConnectionsByUser(animalPost.getUser())) {
            Notification newPostNotification = new Notification();
            newPostNotification.setUser(connection.getConnectedTo());
            newPostNotification.setSender(connection.getUser());
            newPostNotification.setPost(animalPost);
            newPostNotification.setText("There is a new post from your connections");
            newPostNotification.setNotificationType("Post");
            newPostNotification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(newPostNotification);

            notified.add(newPostNotification.getUser());
        }
        return notified;
    }

    private void sendProximityNotifications(Post animalPost, Set<User> notified) {
        for (User user : userRepository.findAll()) {
            if (!notified.contains(user) && !user.equals(animalPost.getUser())) {
                //if (locationHelper.checkProximity(user.getLocation(), user.getNotificationRadius(), animalPost.getLocation())) {
                if (user.getLocation().contains(animalPost.getLocation())) { //TODO fix location
                    Notification newPostNotification = new Notification();
                    newPostNotification.setUser(user);
                    newPostNotification.setSender(animalPost.getUser());
                    newPostNotification.setPost(animalPost);
                    newPostNotification.setText("There is a new post near you");
                    newPostNotification.setNotificationType("Post");
                    newPostNotification.setCreatedAt(LocalDateTime.now());

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
                newCommentNotification.setSender(comment.getUser());
                newCommentNotification.setPost(comment.getPost());
                newCommentNotification.setText("There is a new comment on a post you also commented");
                newCommentNotification.setNotificationType("Comment");
                newCommentNotification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(newCommentNotification);
            }
        }
    }
}
