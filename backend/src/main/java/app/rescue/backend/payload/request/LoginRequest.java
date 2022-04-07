package app.rescue.backend.payload.request;

import lombok.Data;

@Data
public class LoginRequest {
    String email;
    String password;
}
