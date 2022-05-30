package app.rescue.backend.repository;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findConnectionByUserAndConnectedToId(User user, Long connectedToId);

    List<Connection> getAllByConnectedToIdAndConnectionStatus(Long connectedToId, String connectionStatus);

    List<Connection> getAllByUserAndConnectionStatus(User user, String connectionStatus);






    @Query("SELECT c FROM Connection c WHERE c.user = ?1 AND c.connectedToId = ?2 AND c.connectionStatus = 'PENDING'")
    Optional<Connection> getPendingConnection(User user, Long connectedToId);

    @Transactional
    @Modifying
    @Query("UPDATE Connection c SET c.connectionStatus = 'CONNECTED' WHERE c.user = ?1 AND c.connectedToId = ?2")
    void completeConnection(User userOne, Long connectedToId);

    Optional<Connection> findByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Connection c SET c.connectionStatus = 'FOLLOWER' WHERE c.user = ?1")
    void completeRefOrgConnection(User user);

    Collection<Connection> findConnectionsByUser(User user);


}
