package app.rescue.backend.model;

import javax.persistence.*;

@Entity
@Table()
public class Organization extends User {

    @Column(name = "contact_email", unique = true)
    private String contactEmail;

    @Column(name = "region")
    private String region;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "facebook_page_url")
    private String facebookPageUrl;

    @Column(name = "organization_needs")
    private String organizationNeeds;

    public String getOrganizationNeeds() {
        return organizationNeeds;
    }

    public void setOrganizationNeeds(String organization_needs) {
        this.organizationNeeds = organization_needs;
    }

    public String getFacebookPageUrl() {
        return facebookPageUrl;
    }

    public void setFacebookPageUrl(String facebook_page_url) {
        this.facebookPageUrl = facebook_page_url;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String website_url) {
        this.websiteUrl = website_url;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

}