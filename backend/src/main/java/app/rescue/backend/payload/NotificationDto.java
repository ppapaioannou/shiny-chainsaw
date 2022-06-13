package app.rescue.backend.payload;

import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private String sender;
    private String post;
    private Long postId;
    private String text;
    private String createdAt;
    private String readAt;
}
