package app.rescue.backend.model;

import com.vividsolutions.jts.geom.Geometry;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<ConfirmationToken> confirmationTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Comment> comments = new ArrayList<>();

    @ManyToMany(mappedBy = "commentators", cascade = CascadeType.ALL)
    private Collection<Post> commentedOnPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Connection> connections = new ArrayList<>();

    @ManyToMany(mappedBy = "eventAttendees", cascade = CascadeType.ALL)
    private Collection<EventProperties> eventsAttended = new ArrayList<>();

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "locked")
    private Boolean locked = false;

    @Column(name = "enabled")
    private Boolean enabled = false;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "community_standing", nullable = false)
    private Long communityStanding = 1L;

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;

    @Column(name = "referral_token", nullable = false)
    private String referralToken = UUID.randomUUID().toString();

    @Lob
    @Column(name = "location")
    private Geometry location;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private IndividualInformation individualInformation;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrganizationInformation organizationInformation;

    public OrganizationInformation getOrganizationInformation() {
        return organizationInformation;
    }

    public void setOrganizationInformation(OrganizationInformation organizationInformation) {
        this.organizationInformation = organizationInformation;
    }

    public IndividualInformation getIndividualInformation() {
        return individualInformation;
    }

    public void setIndividualInformation(IndividualInformation individualInformation) {
        this.individualInformation = individualInformation;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public String getReferralToken() {
        return referralToken;
    }

    public void setReferralToken(String referralToken) {
        this.referralToken = referralToken;
    }

    public Long getInvitedByUserId() {
        return invitedByUserId;
    }

    public void setInvitedByUserId(Long invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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

    public Collection<EventProperties> getEventsAttended() {
        return eventsAttended;
    }

    public void setEventsAttended(Collection<EventProperties> eventProperties) {
        this.eventsAttended = eventProperties;
    }

    public void setConnections(Collection<Connection> connections) {
        this.connections = connections;
    }

    public Collection<Connection> getConnections() {
        return connections;
    }

    public void setNotifications(Collection<Notification> notifications) {
        this.notifications = notifications;
    }

    public Collection<Notification> getNotifications() {
        return notifications;
    }

    public Collection<Post> getCommentedOnPosts() {
        return commentedOnPosts;
    }

    public void setCommentedOnPosts(Collection<Post> commentedOnPosts) {
        this.commentedOnPosts = commentedOnPosts;
    }

    public void setComments(Collection<Comment> comments) {
        this.comments = comments;
    }

    public Collection<Comment> getComments() {
        return comments;
    }

    public void setPosts(Collection<Post> posts) {
        this.posts = posts;
    }

    public Collection<Post> getPosts() {
        return posts;
    }

    public Collection<Image> getImages() {
        return images;
    }

    public void setImages(Collection<Image> images) {
        this.images = images;
    }

    public Collection<ConfirmationToken> getConfirmationTokens() {
        return confirmationTokens;
    }

    public void setConfirmationTokens(Collection<ConfirmationToken> confirmationTokens) {
        this.confirmationTokens = confirmationTokens;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}