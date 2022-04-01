package app.rescue.backend.payload.request;

import lombok.Data;

import java.util.Collection;

@Data
public class PostRequest {
    //post fields
    private final String title;
    private final Collection<Byte[]> imagesData;
    private final String body;
    private final Boolean enableComments;
    private final String date;
    private final String latitude;
    private final String longitude;

    //animal characteristics
    private final String animalType;
    private final String breed;
    private final String gender;
    private final String size;
    private final String[] colors;
    private final String age;
    private final String microchipNumber;
    private final Boolean neutered;
    private final Boolean goodWithAnimals;
    private final Boolean goodWithChildren;
    private final String actionsTaken;

    //event properties
    private final String time;
    private final String address;
}
