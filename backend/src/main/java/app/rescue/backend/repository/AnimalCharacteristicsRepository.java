package app.rescue.backend.repository;

import app.rescue.backend.model.AnimalCharacteristics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalCharacteristicsRepository extends JpaRepository<AnimalCharacteristics, Long> {
}