package app.rescue.backend.repository;

import app.rescue.backend.model.IndividualInformation;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IndividualInformationRepository extends JpaRepository<IndividualInformation, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE IndividualInformation i SET i.user = ?2 WHERE i = ?1")
    void setIndividualInformationUser(IndividualInformation individualInformation, User user);

}