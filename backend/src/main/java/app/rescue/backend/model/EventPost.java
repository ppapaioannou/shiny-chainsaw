package app.rescue.backend.model;

import org.geolatte.geom.Geometry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;
import java.sql.Time;

@Entity
@Table()
public class EventPost extends Post {
    @Column(name = "address")
    private String eventAddress;

    @Column(name = "location")
    private Geometry eventLocation;

    @Column(name = "date")
    private Date eventDate;

    @Column(name = "time")
    private Time eventTime;

    @Column(name = "enable_discussion", nullable = false)
    private Boolean enableEventDiscussion = true;

    public Boolean getEnableEventDiscussion() {
        return enableEventDiscussion;
    }

    public void setEnableEventDiscussion(Boolean enableDiscussion) {
        this.enableEventDiscussion = enableDiscussion;
    }

    public Time getEventTime() {
        return eventTime;
    }

    public void setEventTime(Time time) {
        this.eventTime = time;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date date) {
        this.eventDate = date;
    }

    public Geometry getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(Geometry location) {
        this.eventLocation = location;
    }

    public String getEventAddress() {
        return eventAddress;
    }

    public void setEventAddress(String address) {
        this.eventAddress = address;
    }
}