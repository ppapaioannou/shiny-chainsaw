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
            //remove the first image, that's the thumbnail, for the carousel to work in the frontend
            images.remove(0);
            return images.stream().map(this::mapFromImageToResponse).collect(Collectors.toList());
        }
        else {
            return null;
        }
    }

    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId).orElseThrow(() ->
                new IllegalStateException("Image does not exits"));
    }

    public Image getProfileImage(User user) {
        return imageRepository.findByUserAndProfileImage(user, true);
    }

    //TODO CHECK EVERYTHING BELOW
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
                Image image = new Image(post, fileName, file.getContentType(), file.getBytes());
                image.setUser(post.getUser());
                imageRepository.save(image);
            }
        }
    }

    private ImageDto mapFromImageToResponse(Image image) {
        String fileDownloadUri = createFileDownloadUri(image);
        return new ImageDto(image.getName(), fileDownloadUri, image.getType(), image.getData().length);
    }

    public String createFileDownloadUri(Image image) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/image/")
                .path(String.valueOf(image.getId()))
                .toUriString();
    }

}
