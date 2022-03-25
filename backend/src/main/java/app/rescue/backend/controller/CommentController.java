package app.rescue.backend.controller;

import app.rescue.backend.model.Comment;
import app.rescue.backend.payload.CommentDto;
import app.rescue.backend.service.CommentService;
import app.rescue.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> createNewComment(@RequestBody CommentDto commentDto) {
        Comment comment = commentService.createNewComment(commentDto);
        notificationService.sendNewCommentNotification(comment);
        return new ResponseEntity<>(comment.toString(), HttpStatus.OK);
    }
}
