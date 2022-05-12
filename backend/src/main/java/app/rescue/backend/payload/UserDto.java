package app.rescue.backend.payload;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String name;
    private String lastName;

    private String accountType;
    private String email;
}
