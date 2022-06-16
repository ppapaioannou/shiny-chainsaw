package app.rescue.backend.controller;

import app.rescue.backend.model.Post;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.PostDto;
import app.rescue.backend.payload.RegistrationDto;
import app.rescue.backend.repository.PostRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.service.PostService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    private User postOwner;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        postOwner = getUser();

        mockPrincipal = mock(Principal.class);
        given(mockPrincipal.getName()).willReturn(postOwner.getEmail());
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canCreateNewAnimalPost() throws Exception {
        // given
        PostDto request = getPostDto();
        String postType = "missing";

        // when
        mvc.perform(post("/api/v1/posts/add-post/" + postType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        Specification<Post> specs = Specification.where(null);
        List<PostDto> posts = postService.getAllPosts(specs, postOwner.getEmail());

        assertEquals(request.getTitle(), posts.get(0).getTitle());
        assertEquals(request.getBody(), posts.get(0).getBody());
        assertEquals(postType, posts.get(0).getPostType());
    }

    @Test
    void canCreateNewEventPost() throws Exception {
        // given
        PostDto request = getPostDto();
        String postType = "event";

        // when
        mvc.perform(post("/api/v1/posts/add-post/" + postType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        Specification<Post> specs = Specification.where(null);
        List<PostDto> posts = postService.getAllPosts(specs, postOwner.getEmail());

        assertEquals(request.getTitle(), posts.get(0).getTitle());
        assertEquals(request.getBody(), posts.get(0).getBody());
        assertEquals(postType, posts.get(0).getPostType());
    }

    @Test
    void canGetAllPostsWithUser() throws Exception {
        // given
        List<Post> expected = new ArrayList<>();
        expected.add(getPost(postOwner));
        expected.add(getPost(postOwner));

        // when
        MvcResult getPostCommentsResult = mvc.perform(get("/api/v1/posts/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostCommentsResult
                .getResponse()
                .getContentAsString();

        List<PostDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(expected.size() -1 -i).getId(), actual.get(i).getId());
            assertEquals(expected.get(expected.size() -1 -i).getTitle(), actual.get(i).getTitle());
            assertEquals(expected.get(expected.size() -1 -i).getBody(), actual.get(i).getBody());
            assertEquals(expected.get(expected.size() -1 -i).getUser().getName(), actual.get(i).getUserName());
        }
    }

    @Test
    void canGetAllPostsWithoutUser() throws Exception {
        // given
        List<Post> expected = new ArrayList<>();
        expected.add(getPost(postOwner));
        expected.add(getPost(postOwner));

        // when
        MvcResult getPostCommentsResult = mvc.perform(get("/api/v1/posts/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostCommentsResult
                .getResponse()
                .getContentAsString();

        List<PostDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );


        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(expected.size() -1 -i).getId(), actual.get(i).getId());
            assertEquals(expected.get(expected.size() -1 -i).getTitle(), actual.get(i).getTitle());
            assertEquals(expected.get(expected.size() -1 -i).getBody(), actual.get(i).getBody());
            assertEquals(expected.get(expected.size() -1 -i).getUser().getName(), actual.get(i).getUserName());
        }
    }

    @Test
    void canGetSinglePostWithUser() throws Exception {
        // given
        Post expected = getPost(postOwner);

        // when
        MvcResult getPostCommentsResult = mvc.perform(get("/api/v1/posts/view/" + expected.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostCommentsResult
                .getResponse()
                .getContentAsString();

        PostDto actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    @Test
    void canGetSinglePostWithoutUser() throws Exception {
        // given
        Post expected = getPost(postOwner);

        // when
        MvcResult getPostCommentsResult = mvc.perform(get("/api/v1/posts/view/" + expected.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostCommentsResult
                .getResponse()
                .getContentAsString();

        PostDto actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    @Test
    void canAttendEvent() throws Exception {
        // given
        RegistrationDto registrationRequest = getRegistrationDto(postOwner.getEmail());
        String userRole = Role.INDIVIDUAL.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        PostDto request = getPostDto();
        String postType = "event";

        mvc.perform(post("/api/v1/posts/add-post/" + postType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        MvcResult getPostsResult = mvc.perform(get("/api/v1/posts/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = getPostsResult
                .getResponse()
                .getContentAsString();

        List<PostDto> postsDto = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        Long eventPostId = postsDto.get(0).getId();

        // when
        mvc.perform(put("/api/v1/posts/event/" + eventPostId + "/attend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        MvcResult getUpdatedPostsResult = mvc.perform(get("/api/v1/posts/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        String updatedContentAsString = getUpdatedPostsResult
                .getResponse()
                .getContentAsString();

        List<PostDto> actual = objectMapper.readValue(
                updatedContentAsString,
                new TypeReference<>() {}
        );

        assertEquals(actual.get(0).getEventAttendees().size(), 1);
    }

    @Test
    void canDeletePost() throws Exception {
        // given
        Post post = getPost(postOwner);
        assertThat(postRepository.existsById(post.getId())).isTrue();

        // when
        mvc.perform(delete("/api/v1/posts/delete/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        //then
        assertThat(postRepository.existsById(post.getId())).isFalse();
    }

    private PostDto getPostDto() {
        PostDto postDto = new PostDto();
        postDto.setId(1L);
        postDto.setTitle("title");
        postDto.setLatitude("");
        postDto.setLongitude("");
        postDto.setColors(new String[]{""});
        postDto.setTime("12:00:00");

        return postDto;
    }

    private User getUser() {
        User user = new User();
        user.setEmail("postOwner@example.com");
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
    private RegistrationDto getRegistrationDto(String email) {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail(email);
        registrationDto.setName("name");
        registrationDto.setPassword("password");

        return registrationDto;
    }


}