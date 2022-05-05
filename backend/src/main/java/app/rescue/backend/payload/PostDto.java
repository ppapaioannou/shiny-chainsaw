package app.rescue.backend.payload;

import app.rescue.backend.model.Image;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class PostDto {
    private Long id;
    private String username;
    private String postType;
    private String createdAt;
    private String distance;

    //post fields
    private String title;
    private String body;
    private Boolean enableComments;
    private String date;
    private String thumbnail;
    private String address;
    private String latitude;
    private String longitude;

    //animal characteristics
    private String animalType;
    private String breed;
    private String gender;
    private String size;
    private String[] colors;
    private String age;
    private String microchipNumber;
    private Boolean neutered;
    private Boolean goodWithAnimals;
    private Boolean goodWithChildren;
    private String actionsTaken;

    //event properties
    private String time;
    //private String address;
}
