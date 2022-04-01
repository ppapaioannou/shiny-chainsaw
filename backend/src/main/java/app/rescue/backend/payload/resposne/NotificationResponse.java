package app.rescue.backend.payload.resposne;

import lombok.Data;

@Data
public class NotificationResponse {
    private String sender;
    private String post;
    private String text;
    private String createdAt;
}
