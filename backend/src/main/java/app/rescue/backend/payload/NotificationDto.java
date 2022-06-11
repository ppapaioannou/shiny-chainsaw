package app.rescue.backend.payload;

import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private String senderName;
    private String post;
    private String text;
    private String createdAt;
    private String readAt;
}
