package app.rescue.backend.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "image")
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Lob
    @Column(name = "data")
    private byte[] data;

    @Column(name = "profile_image", nullable = false)
    private Boolean profileImage = false;

    public Boolean getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Boolean profileImage) {
        this.profileImage = profileImage;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Image(User user, Post post, String name, String type, byte[] data) {
        this.user = user;
        this.post = post;
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public Image(User user, String name, String type, byte[] data) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
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