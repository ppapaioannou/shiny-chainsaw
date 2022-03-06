package app.rescue.backend.model;

import javax.persistence.*;
import java.sql.Blob;
import java.util.*;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "profile_photo")
    private Blob profilePhoto;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "community_standing", nullable = false)
    private Long communityStanding;

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    /*
    idea gia edw
    o xristis pataei ena koumpi "invite a friend",
    auto dimiourgei ena link tou tipou "www.asdasf.com/signup/ref/[user_id]"
    opou user_id to UUID tou xristi,
    auto to link pigainei se mia selida gia signup kai molis oloklirwthei to signug
    me string manipulation
    e.g. k = myStr.lastIndexOf('/'); string.substring(k, string.length() - 1));
    pairnw to id kai to bazw sto pedio edw, meta dimiourgw filia anamesa stous dio xristes
     */
    @Column(name = "invited_by")
    private String invitedBy;

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Long getCommunityStanding() {
        return communityStanding;
    }

    public void setCommunityStanding(Long communityStanding) {
        this.communityStanding = communityStanding;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Blob getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(Blob profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}