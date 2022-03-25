package app.rescue.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name = "event_post")
public class EventPost extends Post {
    @Column(name = "event_address")
    private String eventAddress;

    @Column(name = "event_date")
    private Date eventDate;

    @Column(name = "event_time")
    private Time eventTime;

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

    public String getEventAddress() {
        return eventAddress;
    }

    public void setEventAddress(String address) {
        this.eventAddress = address;
    }
}