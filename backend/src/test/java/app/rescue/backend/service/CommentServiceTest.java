package app.rescue.backend.service;

import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.CommentDto;
import app.rescue.backend.repository.CommentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private CommentService underTest;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        underTest = new CommentService(commentRepository, postService, userService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canAddNewCommentAndAddCommentator() {
        // given
        CommentDto request = getCommentDto();
        String username = "username";

        User user = mock(User.class);
        Post post = mock(Post.class);

        given(userService.getUserByEmail(username)).willReturn(any());
        given(postService.findById(request.getPostId())).willReturn(post);
        given(post.getUser()).willReturn(user);

        // when
        underTest.addNewComment(request, username);

        // then

        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository).save(commentArgumentCaptor.capture());

        Comment capturedComment = commentArgumentCaptor.getValue();

        assertThat(capturedComment.getBody()).isEqualTo(request.getBody());
    }

    @Test
    void canAddNewCommentAndWillNotAddCommentator() {
        // given
        CommentDto request = getCommentDto();
        String username = "username";

        User user = mock(User.class);
        Post post = mock(Post.class);

        given(userService.getUserByEmail(username)).willReturn(user);
        given(postService.findById(request.getPostId())).willReturn(post);
        given(post.getUser()).willReturn(user);

        // when
        underTest.addNewComment(request, username);

        // then

        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository).save(commentArgumentCaptor.capture());

        Comment capturedComment = commentArgumentCaptor.getValue();

        assertThat(capturedComment.getBody()).isEqualTo(request.getBody());
    }

    @Test
    void canGetAllPostComments() {
        // given
        Post post = mock(Post.class);
        Long postId = 1L;
        given(postService.findById(postId)).willReturn(post);

        // when
        underTest.getAllPostComments(postId);
        // then
        verify(commentRepository).findAllCommentsByPost(post);
    }

    @Test
    void canFindById() {
        // given
        Comment comment = mock(Comment.class);
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

        // when
        underTest.findById(comment.getId());

        // then
        verify(commentRepository).findById(comment.getId());
    }

    @Test
    void findByIdWillThrowWhenCommentDoesNotExist() {
        // given
        Comment comment = mock(Comment.class);

        // when
        // then
        assertThatThrownBy(() -> underTest.findById(comment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Comment not found for ID:%s", comment.getId()));
    }

    private CommentDto getCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setPostId(1L);
        commentDto.setBody("body");

        return commentDto;
    }
}