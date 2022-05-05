package app.rescue.backend.payload.resposne;

import lombok.Data;

@Data
public class CommentResponse {
    private String username;
    private String postId;
    private String body;
    private String createdAt;
}
