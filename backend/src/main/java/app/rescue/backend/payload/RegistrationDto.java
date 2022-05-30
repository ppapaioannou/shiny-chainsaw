package app.rescue.backend.payload;

import lombok.Data;

@Data
public class RegistrationDto {
    //common fields
    private String email;
    private String password;
    private String name;
    private String phoneNumber;
    private String description;

    //individual fields
    private String lastName;

    //Organization fields
    private String contactEmail;
    private String latitude;
    private String longitude;
    private String address;
    private String websiteUrl;
    private String facebookPageUrl;
    private String organizationNeeds;

}

