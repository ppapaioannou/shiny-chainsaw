package app.rescue.backend.controller;

import app.rescue.backend.dto.PostDto;
import app.rescue.backend.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(path = "animal/{postType}")
    public ResponseEntity createNewAnimalPost(@RequestBody PostDto postDto, @PathVariable String postType) {
        postService.createNewAnimalPost(postDto, postType);
        return new ResponseEntity(HttpStatus.OK);
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
