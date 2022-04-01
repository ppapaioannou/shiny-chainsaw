package app.rescue.backend.payload.request;

import lombok.Data;

@Data
public class UserLocationRequest {
    private final String latitude;
    private final String longitude;
    private final String diameterInMeters;
}
