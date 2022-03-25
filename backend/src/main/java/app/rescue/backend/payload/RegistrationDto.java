package app.rescue.backend.payload;

import lombok.Data;


@Data
public class RegistrationDto {
    //common fields
    private final String email;
    private final String password;
    private final String name;
    private final Byte[] profileImageData;
    private final String phoneNumber;
    private final String description;

    //individual fields
    private final String lastName;
    //private final String dateOfBirth;

    //Organization fields
    private final String contactEmail;
    private final String region;
    private final String address;
    private final String city;
    private final String zipCode;
    private final String websiteUrl;
    private final String facebookPageUrl;
    private final String organizationNeeds;

}
