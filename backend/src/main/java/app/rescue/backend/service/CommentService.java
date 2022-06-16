package app.rescue.backend.service;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.CommentDto;
import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import app.rescue.backend.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    private final PostService postService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, PostService postService, UserService userService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userService = userService;
    }


    public Comment addNewComment(CommentDto request, String username) {
        User user = userService.getUserByEmail(username);
        Post post = postService.findById(request.getPostId());

        Comment comment = new Comment(user, post, request.getBody());
        commentRepository.save(comment);

        if (!post.getUser().equals(user)) {
            postService.addCommentator(post, user);
        }

        return comment;
    }

    public List<CommentDto> getAllPostComments(Long postId) {
        Post post = postService.findById(postId);
        List<Comment> comments = commentRepository.findAllCommentsByPost(post);
        return comments.stream().map(this::mapFromCommentToResponse).collect(Collectors.toList());
    }

    //TODO public void editComment()

    //TODO public void deleteComment()

    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(String.format("Comment not found for ID:%s", id)));
    }

    private CommentDto mapFromCommentToResponse(Comment comment) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

        CommentDto response = new CommentDto();
        response.setId(comment.getId());
        response.setPostId(comment.getPost().getId());
        response.setBody(comment.getBody());

        User user = comment.getUser();
        response.setUserId(user.getId());
        String userName = user.getName();
        if (comment.getUser().getIndividualInformation() != null) {
            userName +=  " " + user.getIndividualInformation().getLastName();
        }
        response.setUserName(userName);
        response.setCreatedAt(comment.getCreatedAt().format(dateTimeFormatter));

        return response;
    }

}
