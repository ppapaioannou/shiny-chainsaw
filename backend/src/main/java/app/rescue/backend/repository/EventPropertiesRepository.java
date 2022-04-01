package app.rescue.backend.repository;

import app.rescue.backend.model.EventProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventPropertiesRepository extends JpaRepository<EventProperties, Long> {
}