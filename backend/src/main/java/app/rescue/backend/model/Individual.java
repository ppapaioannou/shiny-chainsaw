package app.rescue.backend.model;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "individual")
public class Individual extends User{

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "location")
    private org.geolatte.geom.Geometry location;

    public org.geolatte.geom.Geometry getLocation() {
        return location;
    }

    public void setLocation(org.geolatte.geom.Geometry location) {
        this.location = location;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}