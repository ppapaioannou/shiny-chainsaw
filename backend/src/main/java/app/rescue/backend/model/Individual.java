package app.rescue.backend.model;

import com.vividsolutions.jts.geom.Geometry;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "individual")
public class Individual extends User{

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;
/*
    @Lob
    @Column(name = "location")
    private Geometry location;

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }
 */
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