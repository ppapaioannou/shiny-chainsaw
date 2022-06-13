package app.rescue.backend.service;

import app.rescue.backend.model.Image;
import app.rescue.backend.model.Post;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.ImageDto;
import app.rescue.backend.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepository imageRepository;


    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public List<ImageDto> getPostImages(Post post) {
        Optional<List<Image>> postImages = imageRepository.findByPost(post);

        if (postImages.isPresent()) {
            List<Image> images = postImages.get();
            if (images.size() > 1) {
                //remove the first image, that's the thumbnail, for the carousel to work in the frontend
                images.remove(0);
            }
            return images.stream().map(this::mapFromImageToResponse).collect(Collectors.toList());
        }
        else {
            return null;
        }
    }

    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId).orElseThrow(() ->
                new IllegalStateException("Image does not exist"));
    }

    public Image getProfileImage(User user) {
        return imageRepository.findByUserAndProfileImage(user, true);
    }

    public void storeProfileImage(User user, MultipartFile file) throws IOException {
        Image image;
        if (file != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            image = new Image(user, fileName, file.getContentType(), file.getBytes());
        }
        else {
            if (user
                    .getUserRole()
                    .equals(Role.INDIVIDUAL)) {
                String path = "src/main/resources/images/Individual-Illustration-1.png";

                // tests start from their own folder and always add backend to the path
                // so the get the correct path always we will add it manually if the call
                // comes from the main application
                path = testSanityCheck(path);

                BufferedImage bImage = ImageIO.read(new File(path));
                ByteArrayOutputStream byteOutS = new ByteArrayOutputStream();
                ImageIO.write(bImage, "png", byteOutS);
                byte[] imageInByte = byteOutS.toByteArray();
                image = new Image(user, "Individual-Illustration-1", "image/png", imageInByte);
            }
            else if (user.getUserRole().equals(Role.ORGANIZATION)) {
                String path = "src/main/resources/images/Organization-Illustration-1.png";

                // tests start from their own folder and always add backend to the path
                // so the get the correct path always we will add it manually if the call
                // comes from the main application
                path = testSanityCheck(path);

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
        image.setProfileImage(true);
        imageRepository.save(image);

    }

    public void updateProfileImage(User user, MultipartFile file) throws IOException {
        if (file != null) {
            Image currentImage = imageRepository.findByUserAndProfileImage(user, true);
            imageRepository.delete(currentImage);
            storeProfileImage(user, file);
        }
    }

    public void storePostImages(Post post, MultipartFile[] files) throws IOException {
        if (files != null) {
            for (MultipartFile file : files) {
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                Image image = new Image(post.getUser(), post, fileName, file.getContentType(), file.getBytes());
                imageRepository.save(image);
            }
        }
    }

    public String createFileDownloadUri(Image image) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/image/")
                .path(String.valueOf(image.getId()))
                .toUriString();
    }

    private ImageDto mapFromImageToResponse(Image image) {
        String fileDownloadUri = createFileDownloadUri(image);
        return new ImageDto(image.getName(), fileDownloadUri, image.getType(), image.getData().length);
    }

    private String testSanityCheck(String path) throws IOException {
        String test = new File(path).getCanonicalPath();
        if (!test.contains("backend")) {
            path = "backend/" + path;
        }
        return path;
    }

}
