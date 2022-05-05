package app.rescue.backend.controller;

import app.rescue.backend.model.Image;
import app.rescue.backend.payload.ImageDto;
import app.rescue.backend.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/post-images/{postId}")
    public ResponseEntity<List<ImageDto>> getPostImages(@PathVariable Long postId) {
        List<ImageDto> postImages = imageService.getPostImages(postId).map(postImage -> {
            String fileDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/v1/images/image/")
                    .path(String.valueOf(postImage.getId()))
                    .toUriString();

            return new ImageDto(
                    postImage.getName(),
                    fileDownloadUri,
                    postImage.getType(),
                    postImage.getData().length);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(postImages);
        //Image image = imageService.getPostImages(postId);
        //return ResponseEntity.ok()
        //        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
        //                + image.getName() + "\"").body(image.getData());
    }
/*
    @GetMapping("{postId}")
    public ResponseEntity<List<ResponseFile>> getListFiles() {
        List<ResponseFile> files = storageService.getAllFiles().map(dbFile -> {
            String fileDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    .path(dbFile.getId())
                    .toUriString();
            return new ResponseFile(
                    dbFile.getName(),
                    fileDownloadUri,
                    dbFile.getType(),
                    dbFile.getData().length);
        }).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(files);
*/

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long imageId) {
        Image image = imageService.getImage(imageId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getName() + "\"")
                .body(image.getData());
    }
/*
    @GetMapping("thumbnail/{postId}")
    public ResponseEntity<byte[]> getPostThumbnail(@PathVariable Long postId) {
        Image image = imageService.getPostThumbnail(postId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                        + image.getName() + "\"").body(image.getData());
    }
*/
}
