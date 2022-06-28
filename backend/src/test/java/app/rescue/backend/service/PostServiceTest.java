package app.rescue.backend.service;

import app.rescue.backend.model.EventProperties;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.PostDto;
import app.rescue.backend.repository.AnimalCharacteristicsRepository;
import app.rescue.backend.repository.EventPropertiesRepository;
import app.rescue.backend.repository.PostRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private PostService underTest;

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnimalCharacteristicsRepository animalCharacteristicsRepository;
    @Mock
    private EventPropertiesRepository eventPropertiesRepository;

    @Mock
    private UserService userService;
    @Mock
    private LocationService locationService;
    @Mock
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        underTest = new PostService(postRepository, animalCharacteristicsRepository,
                eventPropertiesRepository, userService, locationService, imageService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canCreateNewAnimalPost() {
        // given
        PostDto request = getPostDto();
        String postType = "missing";
        String username = "username";

        // when
        underTest.createNewPost(request, postType, username);

        // then
        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postArgumentCaptor.capture());

        Post capturedPost = postArgumentCaptor.getValue();

        assertThat(capturedPost.getTitle()).isEqualTo(request.getTitle());
    }

    @Test
    void canCreateNewEventPost() {
        // given
        PostDto request = getPostDto();
        String postType = "event";
        String username = "username";

        // when
        underTest.createNewPost(request, postType, username);

        // then
        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postArgumentCaptor.capture());

        Post capturedPost = postArgumentCaptor.getValue();

        assertThat(capturedPost.getTitle()).isEqualTo(request.getTitle());
    }

    @Test
    void CreateNewPostWillThrowWhenPostTypeIsNotAllowed() {
        // given
        PostDto request = getPostDto();
        String postType = "test";
        String username = "username";

        // when
        // then
        assertThatThrownBy(() -> underTest.createNewPost(request, postType, username))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("%s posts not allowed", postType));

        verify(postRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void canGetAllPosts() {
        // given
        Specification<Post> specs = Specification.where(null);
        Sort sort = Sort.by("id").descending();
        String username = "username";

        //Suppress the unchecked warning for mock classes with generic parameters
        List<Post> posts = mock(List.class);
        given(postRepository.findAll(any(Specification.class), any(Sort.class))).willReturn(posts);
        // when
        underTest.getAllPosts(specs, username);
        // then
        verify(postRepository).findAll(specs, sort);
    }

    @Test
    void canGetSinglePost() {
        // given
        User postOwner = mock(User.class);
        Post post = getPost(postOwner);
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        // when
        //empty username because of nullPointerException on user.getLocation()
        underTest.getSinglePost(anyLong(), "");

        // then
        verify(postRepository).findById(anyLong());
    }

    @Test
    void canAttendEvent() {
        // given
        Post post = mock(Post.class);
        EventProperties eventProperties = mock(EventProperties.class);

        given(post.getEventProperties()).willReturn(eventProperties);
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(post.getPostType()).willReturn("event");

        // when
        underTest.attendEvent(anyLong(), "");

        // then
        verify(eventProperties).addEventAttendee(any());
    }

    @Test
    void canNotAttendEvent() {
        // given
        //User user = mock(User.class);
        Post post = mock(Post.class);
        EventProperties eventProperties = mock(EventProperties.class);

        given(post.getEventProperties()).willReturn(eventProperties);
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(post.getPostType()).willReturn("event");

        given(eventPropertiesRepository
                .existsByPostAndEventAttendeesContaining(any(), any())).willReturn(true);

        // when
        underTest.attendEvent(post.getId(), anyString());

        // then
        verify(eventProperties).removeEventAttendee(any());
    }

    @Test
    void attendEventWillThrowIfPostTypeIsNotEvent() {
        // given
        Post post = mock(Post.class);

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(post.getPostType()).willReturn("test");

        // when
        // then
        assertThatThrownBy(() -> underTest.attendEvent(anyLong(), ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This is not an event");

        verify(postRepository, never()).save(any());
    }

    @Test
    void canDeletePost() {
        // given
        User postOwner = mock(User.class);
        Post post = mock(Post.class);
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(post.getUser()).willReturn(postOwner);
        given(postOwner.getEmail()).willReturn("");

        // when
        underTest.deletePost(post.getId(), anyString());

        // then
        verify(postRepository).delete(any());
    }

    @Test
    void deletePostWillThrowWhenNotPostOwner() {
        // given
        User postOwner = mock(User.class);
        Post post = mock(Post.class);

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(post.getUser()).willReturn(postOwner);
        given(postOwner.getEmail()).willReturn("postOwner");

        // when
        // then
        assertThatThrownBy(() -> underTest.deletePost(post.getId(), "randomUser"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("You don't have permission to delete this post");

        verify(postRepository, never()).delete(any());
    }

    @Test
    void canFindById() {
        // given
        Post post = mock(Post.class);
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

        // when
        underTest.findById(post.getId());

        // then
        verify(postRepository).findById(post.getId());
    }

    @Test
    void findByIdWillThrowWhenPostDoesNotExist() {
        // given
        Post post = mock(Post.class);

        // when
        // then
        assertThatThrownBy(() -> underTest.findById(post.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Post with ID:%s does not exist", post.getId()));

    }

    @Test
    void canAddCommentator() {
        // given
        Post post = mock(Post.class);
        User user = mock(User.class);

        given(post.addCommentator(user)).willReturn(true);

        // when
        underTest.addCommentator(post, user);

        // then
        verify(postRepository).save(any());
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

    private Post getPost(User postOwner) {
        Post post = new Post();
        post.setTitle("title");
        post.setPostType("test");
        post.setUser(postOwner);
        post.setLocation(mock(Geometry.class));
        return post;
    }
}