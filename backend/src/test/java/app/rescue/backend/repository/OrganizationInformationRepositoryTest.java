package app.rescue.backend.repository;

import app.rescue.backend.model.OrganizationInformation;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrganizationInformationRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrganizationInformationRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void setOrganizationInformationUser() {
        User expected = getUser();

        OrganizationInformation organizationInformation = new OrganizationInformation();
        underTest.save(organizationInformation);

        underTest.setOrganizationInformationUser(organizationInformation, expected);

        /*
        Because repository.save will cache the entity. And when you call repository.findByXX,
        JPA will get the entity from the cache not from database. repository.updateXX not update
        the entity.active in the cache. To fix clear the JPA cache
        */
        entityManager.clear();

        OrganizationInformation actual = underTest.getById(organizationInformation.getId());

        assertEquals(expected.getId(), actual.getUser().getId());
    }

    private User getUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.ORGANIZATION);
        userRepository.save(user);
        return user;
    }
}