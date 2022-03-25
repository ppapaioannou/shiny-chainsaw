package app.rescue.backend.payload;

import app.rescue.backend.model.Image;
import app.rescue.backend.model.User;
import com.vividsolutions.jts.geom.Geometry;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {
    Long id;
    String username;
    String title;
    String body;
    String postType;
    String postStatus;
    String createdAt;
    Boolean enableComments;
    String distance;
    //List<Image> images;
    //Geometry location;

    String animalType;
    String breed;
    String[] color;
    String gender;
    String size;

    //String missingDate;
}
