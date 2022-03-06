package app.rescue.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table()
public class SimplePost extends Post {
    @Column(name = "enable_discussion", nullable = false)
    private Boolean enableDiscussion = true;

    public Boolean getEnableDiscussion() {
        return enableDiscussion;
    }

    public void setEnableDiscussion(Boolean enableDiscussion) {
        this.enableDiscussion = enableDiscussion;
    }
}