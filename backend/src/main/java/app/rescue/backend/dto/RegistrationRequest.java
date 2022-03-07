package app.rescue.backend.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
    private final String email;
    private final String password;
    private final String name;
}
