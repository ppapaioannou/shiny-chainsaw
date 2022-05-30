package app.rescue.backend.controller;

import app.rescue.backend.model.Comment;
import app.rescue.backend.payload.CommentDto;
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

    @PostMapping()
    public ResponseEntity<String> addNewComment(@RequestBody CommentDto request, Principal principal) {
        Comment comment = commentService.addNewComment(request, principal.getName());
        notificationService.sendNewCommentNotification(comment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("post/{postId}/all")
    public ResponseEntity<List<CommentDto>> getAllPostComments(@PathVariable Long postId) {
        return new ResponseEntity<>(commentService.getAllPostComments(postId), HttpStatus.OK);
    }

    //TODO @PutMapping(path = "edit/{commentId}") editComment(@PathVariable Long commentId, Principal principal)

    @DeleteMapping(path = {"{commentId}/delete"})
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, Principal principal) {
        commentService.deleteComment(commentId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
