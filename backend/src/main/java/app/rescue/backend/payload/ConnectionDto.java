package app.rescue.backend.payload;

import lombok.Data;

@Data
public class ConnectionDto {
    private Long userId;
    private String name;
    private String profileImage;

    private String lastName;

}
