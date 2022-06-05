package app.rescue.backend.repository;

import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private Post post;
    private final List<Comment> expected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        User postOwner = getUser("postOwner@example.com", Role.INDIVIDUAL);
        post = getPost(postOwner);

        User commentator = getUser("commentator@example.com", Role.INDIVIDUAL);
        User otherCommentator = getUser("otherCommentator@example.com", Role.ORGANIZATION);

        addComment(commentator, post);
        addComment(otherCommentator, post);
        addComment(postOwner, post);
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllCommentsByPost() {
        List<Comment> actual = underTest.findAllCommentsByPost(post);

        assertThat(actual).isNotNull();
        assertThat(expected.size()).isEqualTo(actual.size());

        for (int i = 0; i < expected.size(); i++) {
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

    private Post getPost(User postOwner) {
        Post post = new Post();
        post.setTitle("title");
        post.setPostType("test");
        post.setUser(postOwner);
        postRepository.save(post);
        return post;
    }

    private void addComment(User user, Post post) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setBody("body");
        underTest.save(comment);
        expected.add(comment);
    }

}