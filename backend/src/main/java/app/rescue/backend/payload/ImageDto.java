package app.rescue.backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageDto {
    private String name;
    private String url;
    private String type;
    private long size;
}
