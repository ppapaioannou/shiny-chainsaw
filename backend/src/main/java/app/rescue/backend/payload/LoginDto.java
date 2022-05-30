package app.rescue.backend.payload;

import lombok.Data;

@Data
public class LoginDto {
    String email;
    String password;
}
