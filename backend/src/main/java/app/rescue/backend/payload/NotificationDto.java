package app.rescue.backend.payload;

import lombok.Data;

@Data
public class NotificationDto {
    private String id;
    private String sender;
    private String post;
    private String text;
    private String createdAt;
}
