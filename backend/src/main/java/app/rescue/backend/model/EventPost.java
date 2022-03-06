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
    private String address;

    @Column(name = "location")
    private Geometry location;

    @Column(name = "date")
    private Date date;

    @Column(name = "time")
    private Time time;

    @Column(name = "enable_discussion", nullable = false)
    private Boolean enableDiscussion = true;

    public Boolean getEnableDiscussion() {
        return enableDiscussion;
    }

    public void setEnableDiscussion(Boolean enableDiscussion) {
        this.enableDiscussion = enableDiscussion;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}