package app.rescue.backend.service;

import app.rescue.backend.model.User;
import app.rescue.backend.payload.request.CommentRequest;
import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import app.rescue.backend.payload.resposne.CommentResponse;
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


    public Comment createNewComment(CommentRequest request, Long postId, String userName) {
        Post post = postService.findById(postId);
        User user = userService.getUserByEmail(userName);
        Comment comment = mapFromRequestToComment(request, post);
        comment.setUser(user);
        commentRepository.save(comment);
        if (!post.getUser().equals(user)) {
            postService.addCommentator(post, user);
        }

        return comment;
    }

    public List<CommentResponse> getAllPostComments(Long postId) {
        Post post = postService.findById(postId);
        List<Comment> comments = commentRepository.findAllCommentsByPost(post);
        return comments.stream().map(this::mapFromCommentToResponse).collect(Collectors.toList());
    }

    public void updateComment() {
    }

    public void deleteComment(Long commentId, String userName) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new IllegalStateException(String.format("Comment not found for ID:%s", commentId)));
        if (comment.getUser().getEmail().equals(userName)) {
            commentRepository.delete(comment);
        }
        else {
            throw new IllegalStateException("You don't have permission to delete this comment");
        }
    }

    private Comment mapFromRequestToComment(CommentRequest request, Post post) {
        Comment comment = new Comment();
        if (!post.getEnableComments()) {
            throw new IllegalStateException("Comments are not allowed on this post");
        }
        comment.setPost(post);
        comment.setBody(request.getBody());

        //if (!post.getCommentators().contains(comment.getUser())) {
        //post.addCommentator(comment.getUser());
        //}
        return comment;
    }

    private CommentResponse mapFromCommentToResponse(Comment comment) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setUsername(comment.getUser().getName() + " " + comment.getUser().getIndividualInformation().getLastName());
        commentResponse.setPostId(String.valueOf(comment.getPost().getId()));
        commentResponse.setBody(comment.getBody());
        commentResponse.setCreatedAt(comment.getCreatedAt().format(dateTimeFormatter));

        return commentResponse;
    }
}
