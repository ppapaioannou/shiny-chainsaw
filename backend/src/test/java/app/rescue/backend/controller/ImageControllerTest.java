package app.rescue.backend.controller;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.ImageDto;
import app.rescue.backend.repository.ImageRepository;
import app.rescue.backend.repository.PostRepository;
import app.rescue.backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;




    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ImageRepository imageRepository;


    private User user;
    private Post post;


    @BeforeEach
    void setUp() {
        user = getUser();
        post = getPost(user);
    }

    @AfterEach
    void tearDown() {
        imageRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canGetPostImages() throws Exception{
        //given
        List<Image> expected = new ArrayList<>();

        // first image will be ignored because it's the thumbnail and is not returned from this method
        addPostImage(user, post, "IMAGE DATA 1");
        expected.add(addPostImage(user, post, "IMAGE DATA 2"));
        expected.add(addPostImage(user, post, "IMAGE DATA 3"));
        //when
        MvcResult getPostImagesResult = mvc.perform(get("/api/v1/images/post-images/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostImagesResult
                .getResponse()
                .getContentAsString();

        List<ImageDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getName(), actual.get(i).getName());
            assertEquals(expected.get(i).getType(), actual.get(i).getType());
        }
    }

    @Test
    void canGetFile() throws Exception {
        //given
        Image image = addPostImage(user, post, "IMAGE DATA 1");

        //when
        MvcResult getPostImagesResult = mvc.perform(get("/api/v1/images/image/" + image.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getPostImagesResult
                .getResponse()
                .getContentAsString();

        assertEquals("IMAGE DATA 1", contentAsString);
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

    private Post getPost(User postOwner) {
        Post post = new Post();
        post.setTitle("title");
        post.setPostType("test");
        post.setUser(postOwner);
        postRepository.save(post);
        return post;
    }

    private Image addPostImage(User user, Post post, String imageData) {
        byte[] data = imageData.getBytes();
        Image image = new Image(user, post,"name","type", data);
        imageRepository.save(image);
        return image;
    }
}