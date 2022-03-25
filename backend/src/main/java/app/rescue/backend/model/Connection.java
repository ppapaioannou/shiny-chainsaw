package app.rescue.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "connection")
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "connected_to_id")
    private User connectedTo;

    @Column(name = "connection_status")
    private String connectionStatus;

    @Override
    public String toString() {
        return "Connection{" +
                "id=" + id +
                ", user=" + user.getName() +
                ", connectedTo=" + connectedTo.getName() +
                ", connectionStatus='" + connectionStatus + '\'' +
                '}';
    }

    public Connection(){

    }

    public Connection(User user, User connectedTo, String connectionStatus) {
        this.user = user;
        this.connectedTo = connectedTo;
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public User getConnectedTo() {
        return connectedTo;
    }

    public void setConnectedTo(User connected_to) {
        this.connectedTo = connected_to;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}