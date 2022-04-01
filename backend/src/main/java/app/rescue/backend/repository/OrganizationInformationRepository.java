package app.rescue.backend.repository;

import app.rescue.backend.model.OrganizationInformation;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrganizationInformationRepository extends JpaRepository<OrganizationInformation, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE OrganizationInformation o SET o.user = ?2 WHERE o = ?1")
    void setOrganizationInformationUser(OrganizationInformation organizationInformation, User user);

}