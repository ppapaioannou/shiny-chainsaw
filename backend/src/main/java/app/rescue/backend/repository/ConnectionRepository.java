package app.rescue.backend.repository;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Connection c " + "SET c.connectionStatus = 'CONNECTED' WHERE c.user = ?1 AND c.connectedTo = ?2")
    void connect(User userOne, User userTwo);

    List<Connection> findConnectionsByUser(User user);
}
