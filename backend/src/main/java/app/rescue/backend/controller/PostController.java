package app.rescue.backend.controller;

import app.rescue.backend.model.Post;
import app.rescue.backend.payload.PostDto;
import app.rescue.backend.payload.PostResponse;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.PostService;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final NotificationService notificationService;

    public PostController(PostService postService, NotificationService notificationService) {
        this.postService = postService;
        this.notificationService = notificationService;
    }

    @PostMapping(path = "animal/{postType}")
    public ResponseEntity<String> createNewAnimalPost(@RequestBody PostDto postDto, @PathVariable String postType) {
        Post animalPost = postService.createNewAnimalPost(postDto, postType);
        notificationService.sendNewPostNotification(animalPost);
        return new ResponseEntity<>(animalPost.getTitle(), HttpStatus.OK);
    }

    @PostMapping(path = "simple")
    public ResponseEntity createNewSimplePost(@RequestBody PostDto postDto) {
        postService.createNewSimplePost(postDto);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(path = "event")
    public ResponseEntity createNewEventPost(@RequestBody PostDto postDto) {
        postService.createNewEventPost(postDto);
        //return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(HttpStatus.OK);
    }


    /*
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ) {
        return new ResponseEntity<>(postService.getAllPosts(pageNo, pageSize, sortBy, sortDir), HttpStatus.OK);

    }
    */


    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@SearchSpec Specification<Post> specs) {
        return new ResponseEntity<>(postService.findAll(Specification.where(specs)), HttpStatus.OK);
    }


    /*
    @GetMapping
    public ResponseEntity<List<PostDto>> showAllPosts() {
        return new ResponseEntity<>(postService.showAllPosts(), HttpStatus.OK);
    }
    */
    /*
    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return new ResponseEntity<>(postService.showAllPosts(), HttpStatus.OK);
    }
    */
    /*
    @GetMapping("{postId}")
    public ResponseEntity<PostDto> getSinglePost(@PathVariable @RequestBody Long id) {
        return new ResponseEntity<>(postService.readSinglePost(id), HttpStatus.OK);
    }
    */
}
