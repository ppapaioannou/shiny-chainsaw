package app.rescue.backend.payload.resposne;

import lombok.Data;

@Data
public class PostResponse {
    private Long id;
    private String username;
    private String title;
    private String body;
    private String postType;
    private String date;
    private String createdAt;
    private Boolean enableComments;
    private String distance;
    //List<Image> images;
    //Geometry location;

    private String animalType;
    private String breed;
    private String[] color;
    private String gender;
    private String size;

    //String missingDate;
}
