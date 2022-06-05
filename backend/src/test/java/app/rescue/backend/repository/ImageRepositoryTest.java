package app.rescue.backend.repository;

import app.rescue.backend.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ImageRepositoryTest {

    @Autowired
    private ImageRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private final List<Image> expected = new ArrayList<>();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByPost() {
        User postOwner = getUser("postOwner@example.com");
        Post post = getPost(postOwner);

        addPostImage(postOwner, post);
        addPostImage(postOwner, post);
        addPostImage(postOwner, post);

        Optional<List<Image>> actual = underTest.findByPost(post);

        assertThat(actual).isPresent();
        assertThat(expected.size()).isEqualTo(actual.get().size());

        for (int i = 0; i < actual.get().size(); i++) {
            assertEquals(expected.get(i).getId(), actual.get().get(i).getId());
        }
    }

    @Test
    void findByUserAndProfileImage() {
        User user = getUser("user@example.com");

        Image expected = new Image(user,"name","type",null);
        expected.setProfileImage(true);
        underTest.save(expected);

        Image actual = underTest.findByUserAndProfileImage(user, true);

        assertEquals(expected.getId(), actual.getId());
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

    private void addPostImage(User user, Post post) {
        Image image = new Image(user, post,"name","type",null);
        underTest.save(image);
        expected.add(image);
    }
}