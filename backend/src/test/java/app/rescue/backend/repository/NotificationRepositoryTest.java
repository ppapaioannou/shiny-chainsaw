package app.rescue.backend.repository;

import app.rescue.backend.model.Notification;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private NotificationRepository underTest;

    @Autowired
    private UserRepository userRepository;

    private User user;

    private final List<Notification> expected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user = getUser();

        createNotification(user, "1");
        createNotification(user, "2");
        createNotification(user, "3");
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByUserOrderByIdDesc() {
        List<Notification> actual = underTest.findAllByUserOrderByIdDesc(user);

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(expected.size());

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(expected.size() -1 -i).getId(), actual.get(i).getId());
        }
    }

    @Test
    void getUnreadNotification() {
        expected.get(0).setReadAt(LocalDateTime.now());
        expected.remove(0);

        Optional<List<Notification>> actual = underTest.getUnreadNotification(user);

        assertThat(actual).isPresent();
        assertThat(actual.get().size()).isEqualTo(expected.size());
        
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getId(), actual.get().get(i).getId());
        }
    }

    @Test
    void notificationRead() {
        LocalDateTime expected = LocalDateTime.now();
        Notification notification = this.expected.get(0); //get a random notification for this test
        underTest.notificationRead(notification, expected);

        /*
        Because repository.save will cache the entity. And when you call repository.findByXX,
        JPA will get the entity from the cache not from database. repository.updateXX not update
        the entity.active in the cache. To fix clear the JPA cache
        */
        entityManager.clear();

        Optional<Notification> actual = underTest.findById(notification.getId());
        assertThat(actual).isPresent();
        // when the LocalDateTime is set it is rounded, so the assertion must be if the two object are close
        assertThat(expected).isCloseTo(actual.get().getReadAt(), within(1, ChronoUnit.SECONDS));
    }

    private User getUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.INDIVIDUAL);
        userRepository.save(user);
        return user;
    }

    private void createNotification(User user, String text) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setText(text);
        notification.setNotificationType("test");
        underTest.save(notification);
        expected.add(notification);
    }
}