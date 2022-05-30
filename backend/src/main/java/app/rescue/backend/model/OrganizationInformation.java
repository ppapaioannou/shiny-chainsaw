package app.rescue.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "organization_information")
public class OrganizationInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "contact_email", unique = true)
    private String contactEmail;

    @Column(name = "address")
    private String address;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "facebook_page_url")
    private String facebookPageUrl;

    @Column(name = "organization_needs")
    @Lob
    private String organizationNeeds;

    public String getOrganizationNeeds() {
        return organizationNeeds;
    }

    public void setOrganizationNeeds(String organizationNeeds) {
        this.organizationNeeds = organizationNeeds;
    }

    public String getFacebookPageUrl() {
        return facebookPageUrl;
    }

    public void setFacebookPageUrl(String facebookPageUrl) {
        this.facebookPageUrl = facebookPageUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}