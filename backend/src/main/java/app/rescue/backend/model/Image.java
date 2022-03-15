package app.rescue.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;



    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "image_type")
    private String imageType;

    @Lob
    @Column(name = "data")
    private Byte[] data;

    public Image() {
    }

    public Image(String imageType, Byte[] data) {
        this.imageType = imageType;
        this.data = data;
    }

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Byte[] getData() {
        return data;
    }

    public void setData(Byte[] data) {
        this.data = data;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
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