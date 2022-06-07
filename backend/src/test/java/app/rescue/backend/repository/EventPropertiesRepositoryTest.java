package app.rescue.backend.repository;

import app.rescue.backend.model.EventProperties;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Time;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class EventPropertiesRepositoryTest {

    @Autowired
    private EventPropertiesRepository underTest;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private Post post;
    private User attendee;

    @BeforeEach
    void setUp() {
        post = getPost(getUser("postOwner@example.com"));
        attendee = getUser("user@example.com");
        createEventProperties(post, attendee);
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void existsByPostAndEventAttendeesContaining() {
        boolean actual = underTest
                .existsByPostAndEventAttendeesContaining(post, attendee);
        assertThat(actual).isTrue();
    }

    @Test
    void DoesNotExistByPostAndEventAttendeesContaining() {
        User randomUser = getUser("random@user.com");
        boolean actual = underTest
                .existsByPostAndEventAttendeesContaining(post, randomUser);
        assertThat(actual).isFalse();
    }

    private User getUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.INDIVIDUAL);
        userRepository.save(user);
        return user;
    }

    private Post getPost(User postOwner) {
        Post post = new Post();
        post.setTitle("title");
        post.setPostType("test");
        post.setUser(postOwner);
        postRepository.save(post);
        return post;
    }

    private void createEventProperties(Post post, User attendee) {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setPost(post);
        eventProperties.setTime(Time.valueOf("12:00:00"));
        eventProperties.addEventAttendee(attendee);
        underTest.save(eventProperties);
    }
}