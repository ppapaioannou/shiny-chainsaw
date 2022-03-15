package app.rescue.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table()
public class MissingPost extends AnimalPost {
    @Column(name = "date")
    private Date missingDate;

    @Column(name = "microchip_number")
    private String missingMicrochipNumber;

    public String getMissingMicrochipNumber() {
        return missingMicrochipNumber;
    }

    public void setMissingMicrochipNumber(String microchipNumber) {
        this.missingMicrochipNumber = microchipNumber;
    }

    public Date getMissingDate() {
        return missingDate;
    }

    public void setMissingDate(Date date) {
        this.missingDate = date;
    }
}