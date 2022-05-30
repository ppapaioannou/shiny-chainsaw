package app.rescue.backend.controller;

import app.rescue.backend.model.Image;
import app.rescue.backend.model.Post;
import app.rescue.backend.payload.ImageDto;
import app.rescue.backend.service.ImageService;
import app.rescue.backend.service.PostService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    private final ImageService imageService;
    private final PostService postService;

    public ImageController(ImageService imageService, PostService postService) {
        this.imageService = imageService;
        this.postService = postService;
    }

    @GetMapping("/post-images/{postId}")
    public ResponseEntity<List<ImageDto>> getPostImages(@PathVariable Long postId) {
        Post post = postService.findById(postId);
        return new ResponseEntity<>(imageService.getPostImages(post), HttpStatus.OK);
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long imageId) {
        Image image = imageService.getImage(imageId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getName() + "\"")
                .body(image.getData());
    }

}
