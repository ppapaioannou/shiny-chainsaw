package app.rescue.backend.controller;

import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.CommentDto;
import app.rescue.backend.payload.NotificationDto;
import app.rescue.backend.repository.CommentRepository;
import app.rescue.backend.repository.PostRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.service.CommentService;
import app.rescue.backend.service.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentService commentService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    private Post post;
    private User commentator;
    private User postOwner;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        postOwner = getUser("postOwner@example.com");
        post = getPost(postOwner);
        commentator = getUser("commentator@example.com");

        mockPrincipal = mock(Principal.class);
        given(mockPrincipal.getName()).willReturn("commentator@example.com");
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canAddNewComment() throws Exception {
        // given
        CommentDto request = getCommentDto(post.getId(), commentator);

        // when
        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        List<CommentDto> comments = commentService.getAllPostComments(post.getId());
        assertThat(comments.get(0)).usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(request);

        List<NotificationDto> notifications = notificationService.getAllNotifications(postOwner.getEmail());
        assertThat(notifications.get(0).getSender()).isEqualTo(commentator.getName());
        assertThat(notifications.get(0).getText()).contains("There is a new comment on your post");
    }

    @Test
    void canGetAllPostComments() throws Exception {
        // given
        List<Comment> expected = new ArrayList<>();
        expected.add(addComment(commentator, post));
        expected.add(addComment(postOwner, post));

        // when
        MvcResult getPostCommentsResult = mvc.perform(get("/api/v1/comment/post/"+post.getId()+"/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostCommentsResult
                .getResponse()
                .getContentAsString();

        List<CommentDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getId(), actual.get(i).getId());
            assertEquals(expected.get(i).getPost().getId(), actual.get(i).getPostId());
            assertEquals(expected.get(i).getBody(), actual.get(i).getBody());
            assertEquals(expected.get(i).getUser().getId(), actual.get(i).getUserId());
            assertEquals(expected.get(i).getUser().getName(), actual.get(i).getUserName());
        }
    }

    private CommentDto getCommentDto(Long postId, User user) {
        CommentDto commentDto = new CommentDto();
        commentDto.setPostId(postId);
        commentDto.setUserId(user.getId());
        commentDto.setUserName(user.getName());
        commentDto.setBody("body");

        return commentDto;
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

    private Comment addComment(User user, Post post) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setBody("body");
        commentRepository.save(comment);
        return comment;
    }
}