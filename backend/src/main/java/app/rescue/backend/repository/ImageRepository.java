package app.rescue.backend.repository;

import app.rescue.backend.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllImagesByPostId(Long post_id);

}
