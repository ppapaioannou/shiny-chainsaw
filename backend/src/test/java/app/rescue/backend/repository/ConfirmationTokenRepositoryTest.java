package app.rescue.backend.repository;

import app.rescue.backend.model.ConfirmationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ConfirmationTokenRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ConfirmationTokenRepository underTest;

    private ConfirmationToken expected;


    @BeforeEach
    void setUp() {
        expected = new ConfirmationToken();
        underTest.save(expected);
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findByToken() {
        Optional<ConfirmationToken> actual = underTest.findByToken(expected.getToken());
        assertThat(actual).isPresent();
        assertEquals(expected.getId(), actual.get().getId());
    }

    @Test
    void notFindByToken() {
        Optional<ConfirmationToken> actual = underTest.findByToken(UUID.randomUUID().toString());
        assertThat(actual).isNotPresent();
    }

    @Test
    void updateConfirmedAt() {
        LocalDateTime expected = LocalDateTime.now();
        underTest.updateConfirmedAt(this.expected.getToken(), expected);

        /*
        Because repository.save will cache the entity. And when you call repository.findByXX,
        JPA will get the entity from the cache not from database. repository.updateXX not update
        the entity.active in the cache. To fix clear the JPA cache
        */
        entityManager.clear();

        Optional<ConfirmationToken> actual = underTest.findByToken(this.expected.getToken());
        assertThat(actual).isPresent();
        // when the LocalDateTime is set it is rounded, so the assertion must be if the two object are close
        assertThat(expected).isCloseTo(actual.get().getConfirmedAt(), within(1, ChronoUnit.SECONDS));
    }

}