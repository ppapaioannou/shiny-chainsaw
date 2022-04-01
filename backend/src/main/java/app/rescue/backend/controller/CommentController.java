package app.rescue.backend.controller;

import app.rescue.backend.model.Comment;
import app.rescue.backend.payload.request.CommentRequest;
import app.rescue.backend.payload.resposne.CommentResponse;
import app.rescue.backend.service.CommentService;
import app.rescue.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1/comment")
public class CommentController {

    private final CommentService commentService;
    private final NotificationService notificationService;

    public CommentController(CommentService commentService, NotificationService notificationService) {
        this.commentService = commentService;
        this.notificationService = notificationService;
    }

    @PostMapping(path = "add/{postId}")
    public ResponseEntity<String> createNewComment(@RequestBody CommentRequest request,
                                                   @PathVariable Long postId, Principal principal) {
        Comment comment = commentService.createNewComment(request, postId, principal.getName());
        notificationService.sendNewCommentNotification(comment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("post-comments/{postId}")
    public ResponseEntity<List<CommentResponse>> getAllPostComments(@PathVariable Long postId) {
        return new ResponseEntity<>(commentService.getAllPostComments(postId), HttpStatus.OK);
    }

    @PutMapping(path = "{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long commentId, Principal principal) {
        commentService.updateComment();
        //notificationService.sendNewCommentNotification(comment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = {"delete/{commentId}"})
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, Principal principal) {
        commentService.deleteComment(commentId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
