package app.rescue.backend.repository;

import app.rescue.backend.model.Image;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<List<Image>> findByPost(Post post);


    Image findByUserAndProfileImage(User user, Boolean profileImage);

}