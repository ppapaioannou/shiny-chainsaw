package app.rescue.backend.repository;

import app.rescue.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


//@Repository
//public interface PostRepository extends JpaRepository<Post, Long> {

    /*
    @Transactional
    @Modifying
    @Query("UPDATE Post p " + "SET p.distance = ?2 WHERE p.id = ?1")
    int setPostDistance(Long id, Double distance);
    */
//}
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

}