package app.rescue.backend.repository;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ConnectionRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ConnectionRepository underTest;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeEach
    void setUp() {
        user1 = getUser("user1@example.com", Role.INDIVIDUAL);
        user2 = getUser("user2@example.com", Role.INDIVIDUAL);
        user3 = getUser("user3@example.com", Role.INDIVIDUAL);
        user4 = getUser("user4@example.com", Role.ORGANIZATION);
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findConnectionByUserAndConnectedToId() {
        Connection expected = new Connection(user1, user2.getId(), "test");
        underTest.save(expected);

        Optional<Connection> actual = underTest.findConnectionByUserAndConnectedToId(user1, user2.getId());
        assertThat(actual).isPresent();
        assertEquals(expected.getId(), actual.get().getId());
    }

    @Test
    void notFindConnectionByUserAndConnectedToId() {
        Optional<Connection> actual = underTest.findConnectionByUserAndConnectedToId(user1, -1L);
        assertThat(actual).isNotPresent();
    }

    @Test
    void getAllByConnectedToIdAndConnectionStatus() {
        List<Connection> expected = new ArrayList<>();

        Connection connection1 = new Connection(user2, user1.getId(), "test");
        underTest.save(connection1);
        expected.add(connection1);

        Connection connection2 = new Connection(user3, user1.getId(), "test");
        underTest.save(connection2);
        expected.add(connection2);

        List<Connection> actual = underTest.getAllByConnectedToIdAndConnectionStatus(user1.getId(), "test");

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(expected.size());

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i).getId(), actual.get(i).getId());
        }
    }

    @Test
    void getAllByUserAndConnectionStatus() {
        List<Connection> expected = new ArrayList<>();

        Connection connection1 = new Connection(user1, user2.getId(), "test");
        underTest.save(connection1);
        expected.add(connection1);

        Connection connection2 = new Connection(user1, user3.getId(), "test");
        underTest.save(connection2);
        expected.add(connection2);

        List<Connection> actual = underTest.getAllByUserAndConnectionStatus(user1, "test");

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(expected.size());

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    void findPendingConnection() {
        Connection expected = new Connection(user1, user2.getId(), "PENDING");
        underTest.save(expected);

        Optional<Connection> actual = underTest.findPendingConnection(user1, user2.getId());

        assertThat(actual).isPresent();
        assertEquals(expected, actual.get());
    }

    @Test
    void notFindPendingConnection() {
        Optional<Connection> actual = underTest.findPendingConnection(user1, -1L);

        assertThat(actual).isNotPresent();
    }

    @Test
    void completeConnection() {
        Connection expected = new Connection(user1, user2.getId(), "PENDING");
        underTest.save(expected);

        underTest.completeConnection(user1, user2.getId());

        /*
        Because repository.save will cache the entity. And when you call repository.findByXX,
        JPA will get the entity from the cache not from database. repository.updateXX not update
        the entity.active in the cache. To fix clear the JPA cache
        */
        entityManager.clear();

        Optional<Connection> actual = underTest.findConnectionByUserAndConnectedToId(user1, user2.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getConnectionStatus()).isEqualTo("CONNECTED");
    }

    @Test
    void findRefPendingConnection() {
        Connection expected = new Connection(user1, user2.getId(), "REF-PENDING");
        underTest.save(expected);

        Optional<Connection> actual = underTest.findRefPendingConnection(user1);

        assertThat(actual).isPresent();
        assertEquals(expected.getId(), actual.get().getId());
    }

    @Test
    void findRefFollowerConnection() {
        Connection expected = new Connection(user1, user2.getId(), "REF-FOLLOWER");
        underTest.save(expected);

        Optional<Connection> actual = underTest.findRefPendingConnection(user1);

        assertThat(actual).isPresent();
        assertEquals(expected.getId(), actual.get().getId());
    }

    @Test
    void notFindRefPendingConnection() {
        Optional<Connection> actual = underTest.findRefPendingConnection(user1);

        assertThat(actual).isNotPresent();
    }

    @Test

    void completeRefOrgConnection() {
        Connection expected = new Connection(user1, user4.getId(), "REF-FOLLOWER");
        underTest.save(expected);

        underTest.completeRefOrgConnection(user1);

        /*
        Because repository.save will cache the entity. And when you call repository.findByXX,
        JPA will get the entity from the cache not from database. repository.updateXX not update
        the entity.active in the cache. To fix clear the JPA cache
        */
        entityManager.clear();

        Optional<Connection> actual = underTest.findConnectionByUserAndConnectedToId(user1, user4.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getConnectionStatus()).isEqualTo("FOLLOWER");
    }

    @Test
    void findConnectionsByUser() {
        List<Connection> expected = new ArrayList<>();

        Connection connection1 = new Connection(user1, user2.getId(), "test");
        underTest.save(connection1);
        expected.add(connection1);

        Connection connection2 = new Connection(user1, user3.getId(), "test");
        underTest.save(connection2);
        expected.add(connection2);

        List<Connection> actual = underTest.findConnectionsByUser(user1);

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(expected.size());

        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i).getId(), actual.get(i).getId());
        }
    }

    private User getUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(role);
        userRepository.save(user);
        return user;
    }

}