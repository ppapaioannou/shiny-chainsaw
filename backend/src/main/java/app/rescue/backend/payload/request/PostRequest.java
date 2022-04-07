package app.rescue.backend.payload.request;

import lombok.Data;

import java.util.Collection;

@Data
public class PostRequest {
    //post fields
    private String title;
    private Collection<Byte[]> imagesData;
    private String body;
    private Boolean enableComments;
    private String date;
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
    private String address;
}
