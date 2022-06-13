package app.rescue.backend.repository;

import app.rescue.backend.model.Notification;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserOrderByIdDesc(User user);

    @Query("SELECT n FROM Notification n WHERE n.user = ?1 AND n.readAt IS NULL")
    Optional<List<Notification>> getUnreadNotification(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = ?2 WHERE n = ?1")
    void notificationRead(Notification notification, LocalDateTime readAt);
}
