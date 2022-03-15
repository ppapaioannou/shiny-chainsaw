package app.rescue.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table()
public class StrayPost extends AnimalPost {
    @Column(name = "date")
    private Date strayDate;

    @Column(name = "actions_taken")
    private String actionsTaken;

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public Date getStrayDate() {
        return strayDate;
    }

    public void setStrayDate(Date date) {
        this.strayDate = date;
    }
}