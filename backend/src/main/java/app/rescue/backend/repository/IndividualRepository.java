package app.rescue.backend.repository;

import app.rescue.backend.model.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndividualRepository extends UserRepository {
}