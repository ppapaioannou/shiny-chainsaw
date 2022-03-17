package app.rescue.backend.controller;

import app.rescue.backend.dto.CommentDto;
import app.rescue.backend.service.CommentService;
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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping()
    public ResponseEntity createNewComment(@RequestBody CommentDto commentDto) {
        commentService.createNewComment(commentDto);
        return new ResponseEntity(HttpStatus.OK);
    }
}
