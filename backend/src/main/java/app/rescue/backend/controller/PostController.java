package app.rescue.backend.controller;

import app.rescue.backend.model.Post;
import app.rescue.backend.payload.PostDto;
import app.rescue.backend.service.ImageService;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.PostService;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final ImageService imageService;
    private final NotificationService notificationService;

    public PostController(PostService postService, ImageService imageService, NotificationService notificationService) {
        this.postService = postService;
        this.imageService = imageService;
        this.notificationService = notificationService;
    }

    @PostMapping(path = "add-post/{postType}")
    public ResponseEntity<String> createNewPost(@RequestParam("payload") PostDto request,
                                                @RequestParam(value = "file", required = false) MultipartFile[] images,
                                                @PathVariable String postType, Principal principal) throws IOException {
        Post post = postService.createNewPost(request, postType, principal.getName());
        imageService.storePostImages(post, images);
        notificationService.sendNewPostNotification(post);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "all")
    public ResponseEntity<List<PostDto>> getAllPosts(@SearchSpec Specification<Post> specs, Principal principal) {
        // if no user is logged in then don't display location information
        if (principal != null) {
            return new ResponseEntity<>(postService.getAllPosts(Specification.where(specs), principal.getName()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(postService.getAllPosts(Specification.where(specs), ""), HttpStatus.OK);
        }

    }

    @GetMapping(path = "view/{postId}")
    public ResponseEntity<PostDto> getSinglePost(@PathVariable Long postId, Principal principal) {
        if (principal != null) {
            return new ResponseEntity<>(postService.getSinglePost(postId, principal.getName()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(postService.getSinglePost(postId, ""), HttpStatus.OK);
        }
    }

    //TODO @PutMapping(path = "edit/{postId}") editPost(@PathVariable Long postId, Principal principal)

    @PutMapping(path = "event/{postId}/attend")
    public ResponseEntity<String> attendEvent(@PathVariable Long postId, Principal principal) {
        postService.attendEvent(postId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "delete/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, Principal principal) {
        postService.deletePost(postId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
