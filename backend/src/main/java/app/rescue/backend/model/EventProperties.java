package app.rescue.backend.model;

import javax.persistence.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "event_properties")
public class EventProperties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToMany
    @JoinTable(name = "event_attendees", joinColumns = @JoinColumn(name = "event_properties_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"))
    private Collection<User> eventAttendees = new ArrayList<>();

    @Column(name = "time", nullable = false)
    private Time time;

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public void addEventAttendee(User user) {
        eventAttendees.add(user);
    }

    public void removeEventAttendee(User user) {
        eventAttendees.remove(user);
    }

    public Collection<User> getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(Collection<User> eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}