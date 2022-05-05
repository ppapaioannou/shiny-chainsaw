package app.rescue.backend.service;

import app.rescue.backend.model.Image;
import app.rescue.backend.model.Post;
import app.rescue.backend.payload.ImageDto;
import app.rescue.backend.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    private final PostService postService;

    public ImageService(ImageRepository imageRepository, PostService postService) {
        this.imageRepository = imageRepository;
        this.postService = postService;
    }

    public void storePostImage(Post post, MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Image image = new Image(post, fileName, file.getContentType(), file.getBytes());
        imageRepository.save(image);
    }

    public Stream<Image> getPostImages(Long postId) {
        Post post = postService.findById(postId);
        Optional<List<Image>> postImages = imageRepository.findByPost(post);
        return postImages.map(Collection::stream).orElse(null);

        //return imageRepository.findAll().stream();
        //return imageRepository.findByPost(post).stream();
        //return imageRepository.findById(id).orElseThrow(() ->
        //        new IllegalStateException("Image does not exits"));
    }

    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId).orElseThrow(() ->
                new IllegalStateException("Image does not exits"));
    }
}
