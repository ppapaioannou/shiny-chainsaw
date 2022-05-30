package app.rescue.backend.payload;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private Long postId;
    private String body;
    private Long userId;
    private String userName;
    private String createdAt;
}
