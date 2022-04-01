package app.rescue.backend.repository;

import app.rescue.backend.model.Comment;
import app.rescue.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.post = ?1")
    List<Comment> findAllCommentsByPost(Post post);
}
