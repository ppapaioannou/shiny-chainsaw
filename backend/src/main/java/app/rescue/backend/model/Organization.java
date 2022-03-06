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
    private String website_url;

    @Column(name = "facebook_page_url")
    private String facebook_page_url;

    @Column(name = "organization_needs")
    private String organization_needs;

    public String getOrganization_needs() {
        return organization_needs;
    }

    public void setOrganization_needs(String organization_needs) {
        this.organization_needs = organization_needs;
    }

    public String getFacebook_page_url() {
        return facebook_page_url;
    }

    public void setFacebook_page_url(String facebook_page_url) {
        this.facebook_page_url = facebook_page_url;
    }

    public String getWebsite_url() {
        return website_url;
    }

    public void setWebsite_url(String website_url) {
        this.website_url = website_url;
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