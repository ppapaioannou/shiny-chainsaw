package app.rescue.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "event_attendees")
public class EventAttendees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_post_id")
    private EventPost eventPost;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "response", nullable = false)
    private Boolean response = false;

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EventPost getEventPost() {
        return eventPost;
    }

    public void setEventPost(EventPost eventPost) {
        this.eventPost = eventPost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}