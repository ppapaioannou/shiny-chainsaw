package app.rescue.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostDto {
    //common fields
    private final String title;
    private final List<Byte[]> imagesData;
    private final String body;
    private final String postStatus;
    private final Boolean enableComments;

    //event fields
    private final String eventAddress;
    private final String eventLocation; //Geometry
    private final String eventDate;
    private final String eventTime;
    //private final Boolean enableEventDiscussion;

    //simple fields
    //private final Boolean enableDiscussion;

    //animal fields
    private final String animalType;
    private final String animalLocation;
    private final String breed;
    private final String gender;
    private final String color;
    private final String size;

    //missing fields
    private final String missingDate;
    private final String missingMicrochipNumber;

    //adoption fields
    private final String age;
    private final Boolean neutered;
    private final String adoptionMicrochipNumber;
    private final Boolean goodWithChildren;
    private final Boolean goodWithAnimals;

    //stray fields
    private final String strayDate;
    private final String actionsTaken;
}
