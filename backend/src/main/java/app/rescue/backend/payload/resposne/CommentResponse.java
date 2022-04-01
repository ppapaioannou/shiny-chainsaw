package app.rescue.backend.payload.resposne;

import lombok.Data;

@Data
public class CommentResponse {
    private String name;
    private String postId;
    private String body;
    private String createdAt;
}
