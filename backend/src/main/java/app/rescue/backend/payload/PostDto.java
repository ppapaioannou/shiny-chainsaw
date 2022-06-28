package app.rescue.backend.payload;

import lombok.Data;

import java.util.List;

@Data
public class PostDto {
    //common
    private Long id;
    private String title;
    private String body;
    private String postType;
    private String createdAt;
    private int numberOfComments;
    private String date;
    private String thumbnail;
    private String address;
    private String latitude;
    private String longitude;
    private Double distance;
    private String userName;
    private String userId;

    //animal characteristics
    private String animalType;
    private String breed;
    private String gender;
    private String size;
    private String[] colors;
    private String age;
    private String microchipNumber;
    private String neutered;
    private String goodWithAnimals;
    private String goodWithChildren;
    private String actionTaken;

    //event properties
    private String time;
    private List<UserDto> eventAttendees;
    //private String address;
}
