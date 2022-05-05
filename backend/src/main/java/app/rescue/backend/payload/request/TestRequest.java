package app.rescue.backend.payload.request;

import lombok.Data;

@Data
public class TestRequest {
    private String id;
    private String title;
    private String body;
    private String username;
}
