package app.rescue.backend.payload;

import lombok.Data;

@Data
public class UserDto {
    //common
    private Long id;
    private String name;
    private String accountType;
    private String email;
    private String profileImage;
    private String phoneNumber;
    private String description;
    private String communityStanding;

    //individual
    private String lastName;
    private String dateOfBirth;


    //organization
    private String contactEmail;
    private String websiteUrl;
    private String facebookPageUrl;
    private String organizationNeeds;
}
