package app.rescue.backend.repository;

import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository underTest;

    private User expected;

    @BeforeEach
    void setUp() {
        expected = getUser();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findByEmail() {
        Optional<User> actual = underTest.findByEmail(expected.getEmail());
        if (actual.isPresent()) {
            assertEquals(expected, actual.get());
        }
        else {
            throw new IllegalStateException("User not found by email");
        }
    }

    @Test
    void notFindByEmail() {
        Optional<User> actual = underTest.findByEmail("random@email.example");
        assertThat(actual).isNotPresent();
    }

    @Test
    void enableUser() {
        assertThat(expected.isEnabled()).isFalse();

        underTest.enableUser(expected.getEmail());

        /*
        Because repository.save will cache the entity. And when you call repository.findByXX,
        JPA will get the entity from the cache not from database. repository.updateXX not update
        the entity.active in the cache. To fix clear the JPA cache
        */
        entityManager.clear();

        Optional<User> actual = underTest.findById(expected.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().isEnabled()).isTrue();
    }

    @Test
    void findByReferralToken() {
        Optional<User> actual = underTest.findByReferralToken(expected.getReferralToken());
        if (actual.isPresent()) {
            assertEquals(expected, actual.get());
        }
        else {
            throw new IllegalStateException("User not found by referral token");
        }

        Optional<User> notExpected = underTest.findByReferralToken(UUID.randomUUID().toString());
        assertThat(notExpected).isNotPresent();
    }

    @Test
    void notFindByReferralToken() {
        Optional<User> actual = underTest.findByReferralToken(UUID.randomUUID().toString());
        assertThat(actual).isNotPresent();
    }
/*
    @Test
    void existsByEmail() {
        boolean actual = underTest.existsByEmail(expected.getEmail());
        assertThat(actual).isTrue();
    }

    @Test
    void notExistsByEmail() {
        boolean actual = underTest.existsByEmail(new User().getEmail());
        assertThat(actual).isFalse();
    }
*/
    @Test
    void existsByEmailAndEnabled() {
        underTest.enableUser(expected.getEmail());
        entityManager.clear();
        boolean actual = underTest.existsByEmailAndEnabled(expected.getEmail(), true);
        assertThat(actual).isTrue();
    }

    @Test
    void notExistsByEmailAndEnabled() {
        boolean actual = underTest.existsByEmailAndEnabled(expected.getEmail(), true);
        assertThat(actual).isFalse();
    }

    private User getUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.INDIVIDUAL);
        underTest.save(user);
        return user;
    }
}