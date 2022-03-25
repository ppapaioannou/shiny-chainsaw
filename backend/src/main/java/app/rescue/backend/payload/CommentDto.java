package app.rescue.backend.payload;

import lombok.Data;

@Data
public class CommentDto {
    private final String postId;
    private final String text;
}
