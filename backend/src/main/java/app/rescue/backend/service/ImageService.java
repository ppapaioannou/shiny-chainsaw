package app.rescue.backend.service;

import app.rescue.backend.model.Image;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

    public void storeProfileImage(User user, MultipartFile file) throws IOException {
        Image image;
        if (file != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            image = new Image(user, fileName, file.getContentType(), file.getBytes());

        }
        else {
            if (user.getUserRole().equals(Role.INDIVIDUAL)) {
                String path = "backend/src/main/resources/images/Individual-Illustration-1.png";
                BufferedImage bImage = ImageIO.read(new File(path));
                ByteArrayOutputStream byteOutS = new ByteArrayOutputStream();
                ImageIO.write(bImage, "png", byteOutS);
                byte[] imageInByte = byteOutS.toByteArray();
                image = new Image(user, "Individual-Illustration-1", "image/png", imageInByte);
            }
            else if (user.getUserRole().equals(Role.ORGANIZATION)) {
                String path = "backend/src/main/resources/images/Organization-Illustration-1.png";
                BufferedImage bImage = ImageIO.read(new File(path));
                ByteArrayOutputStream byteOutS = new ByteArrayOutputStream();
                ImageIO.write(bImage, "png", byteOutS);
                byte[] imageInByte = byteOutS.toByteArray();
                image = new Image(user, "Organization-Illustration-1", "image/png", imageInByte);
            }
            else {
                throw new IllegalStateException("Not an user role.");
            }
        }
        imageRepository.save(image);

    }

    public void storePostImage(Post post, MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Image image = new Image(post, fileName, file.getContentType(), file.getBytes());
        image.setUser(post.getUser());
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
