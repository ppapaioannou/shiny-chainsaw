package app.rescue.backend.service;

import app.rescue.backend.dto.CommentDto;
import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import app.rescue.backend.repository.CommentRepository;
import app.rescue.backend.repository.PostRepository;
import app.rescue.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserService userService, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public void createNewComment(CommentDto commentDto) {
        Comment comment = mapFromDtoToComment(commentDto);
        commentRepository.save(comment);
    }

    private Comment mapFromDtoToComment(CommentDto commentDto) {
        Comment comment = new Comment();
        Post post = postRepository.getById(Long.valueOf(commentDto.getPostId()));
        if (post.getEnableComments() != true) {
            throw new IllegalStateException("comments are not allowed on this post");
        }
        comment.setPost(post);
        comment.setText(commentDto.getText());
        comment.setUser(userRepository.findUserByEmail(userService.getCurrentUser()));
        return comment;
    }
}
