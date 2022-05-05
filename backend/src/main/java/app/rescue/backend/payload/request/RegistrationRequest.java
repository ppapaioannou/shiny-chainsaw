package app.rescue.backend.payload.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Data
public class RegistrationRequest {
    //common fields
    private String email;
    private String password;
    private String name;
    //private Byte[] profileImage;
    //private MultipartFile profileImage;
    private String phoneNumber;
    private String description;

    //individual fields
    private String lastName;
    //private final String dateOfBirth;

    //Organization fields
    private String contactEmail;
    private String region;
    private String address;
    private String city;
    private String zipCode;
    private String websiteUrl;
    private String facebookPageUrl;
    private String organizationNeeds;

}
