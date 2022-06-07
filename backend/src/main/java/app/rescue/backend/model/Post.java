package app.rescue.backend.model;

import com.vividsolutions.jts.geom.Geometry;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Comment> comments = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "commentators", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Collection<User> commentators = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Notification> notificationsSent = new ArrayList<>();

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body")
    @Lob
    private String body;

    @Column(name = "post_type", nullable = false)
    private String postType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnimalCharacteristics animalCharacteristics;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventProperties eventProperties;

    @Column(name = "date")
    private Date date = Date.valueOf(LocalDate.now());

    @Lob
    @Column(name = "location")
    private Geometry location;

    @Column(name = "address")
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setComments(Collection<Comment> comments) {
        this.comments = comments;
    }

    public Collection<Comment> getComments() {
        return comments;
    }

    //@Lob
    //@ElementCollection
    //@Column(name = "image")
    //@CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    //private byte[] image;

    //public byte[] getImages() {
    //    return image;
    //}

    //public void setImages(byte[] image) {
    //    this.image = image;
    //}

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public EventProperties getEventProperties() {
        return eventProperties;
    }

    public void setEventProperties(EventProperties eventProperties) {
        this.eventProperties = eventProperties;
    }

    public AnimalCharacteristics getAnimalCharacteristics() {
        return animalCharacteristics;
    }

    public void setAnimalCharacteristics(AnimalCharacteristics animalCharacteristics) {
        this.animalCharacteristics = animalCharacteristics;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Collection<Notification> getNotificationsSent() {
        return notificationsSent;
    }

    public void setNotificationsSent(Collection<Notification> notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    public boolean addCommentator(User user) {
        if (!commentators.contains(user)) {
            commentators.add(user);
            return true;
        }
        return false;
    }
    public Collection<User> getCommentators() {
        return commentators;
    }

    public void setCommentators(Collection<User> commentators) {
        this.commentators = commentators;
    }

    public Collection<Image> getImages() {
        return images;
    }

    public void setImages(Collection<Image> images) {
        this.images = images;
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