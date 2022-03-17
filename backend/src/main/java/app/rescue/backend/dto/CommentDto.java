package app.rescue.backend.dto;

import lombok.Data;

@Data
public class CommentDto {
    private final String postId;
    private final String text;
}
