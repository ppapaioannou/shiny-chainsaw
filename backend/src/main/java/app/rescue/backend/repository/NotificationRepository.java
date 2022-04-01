package app.rescue.backend.repository;

import app.rescue.backend.model.Notification;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = ?2 WHERE n = ?1")
    void notificationRead(Notification notification, LocalDateTime readAt);
}
