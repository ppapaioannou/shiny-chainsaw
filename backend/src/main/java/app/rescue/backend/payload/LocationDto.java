package app.rescue.backend.payload;

import lombok.Data;

@Data
public class LocationDto {
    private String latitude;
    private String longitude;
    private String address;
    private String diameterInMeters;
}
