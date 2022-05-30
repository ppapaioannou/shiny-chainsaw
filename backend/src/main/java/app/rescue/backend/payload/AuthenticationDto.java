package app.rescue.backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationDto {
    private String authenticationToken;
    private String email;
    private Long id;
    private String role;
}
