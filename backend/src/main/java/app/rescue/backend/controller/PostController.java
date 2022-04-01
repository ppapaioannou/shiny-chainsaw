package app.rescue.backend.controller;

import app.rescue.backend.model.Post;
import app.rescue.backend.payload.request.PostRequest;
//import app.rescue.backend.service.NotificationService;
import app.rescue.backend.payload.resposne.PostResponse;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.PostService;
import app.rescue.backend.util.AppConstants;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @PostMapping(path = "new-post/{postType}")
    public ResponseEntity<String> createNewPost(@RequestBody PostRequest request,
                                                      @PathVariable String postType, Principal principal) {
        Post post = postService.createNewPost(request, postType, principal.getName());
        notificationService.sendNewPostNotification(post);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @SearchSpec Specification<Post> specs) {
        return new ResponseEntity<>(postService.getAllPosts(pageNo, pageSize, sortBy, sortDir,
                Specification.where(specs)), HttpStatus.OK);
    }

    @GetMapping(path = "{postId}")
    public ResponseEntity<PostResponse> getSinglePost(@PathVariable Long postId) {
        return new ResponseEntity<>(postService.getSinglePost(postId), HttpStatus.OK);
    }

    @PutMapping(path = "{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId, Principal principal) {
        postService.updatePost(postId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "event/{postId}/attend")
    public ResponseEntity<String> willAttendEvent(@PathVariable Long postId, Principal principal) {
        postService.willAttendEvent(postId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, Principal principal) {
        postService.deletePost(postId, principal.getName());
        return new ResponseEntity<>(HttpStatus.OK);
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


    //@GetMapping
    //public ResponseEntity<List<PostResponse>> getAllPosts(@SearchSpec Specification<Post> specs) {
    //    return new ResponseEntity<>(postService.findAll(Specification.where(specs)), HttpStatus.OK);
    //}


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
