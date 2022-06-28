package app.rescue.backend.service;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.ImageDto;
import app.rescue.backend.repository.ImageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private ImageService underTest;

    @Mock
    private ImageRepository imageRepository;

    @BeforeEach
    void setUp() {
        underTest = new ImageService(imageRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canGetPostImages() {
        // given
        Post post = mock(Post.class);
        User user = mock(User.class);

        List<Image> postImages = new ArrayList<>();

        postImages.add(addPostImage(user, post, "IMAGE DATA 1"));
        postImages.add(addPostImage(user, post, "IMAGE DATA 2"));

        given(imageRepository.findByPost(post)).willReturn(Optional.of(postImages));


        // when
        List<ImageDto> actual = underTest.getPostImages(post);

        // then
        assertThat(actual).isNotNull();
    }

    @Test
    void getPostImagesReturnsNullIfPostHasNoImages() {
        // given
        Post post = mock(Post.class);

        // when
        List<ImageDto> actual = underTest.getPostImages(post);

        // then
        assertThat(actual).isNull();
    }

    @Test
    void canGetImage() {
        // given
        Image image = mock(Image.class);
        given(imageRepository.findById(anyLong())).willReturn(Optional.of(image));

        // when
        underTest.getImage(image.getId());

        // then
        verify(imageRepository).findById(anyLong());
    }

    @Test
    void getImageWillThrowWhenImageDoesNotExist() {
        // given
        Image image = mock(Image.class);

        // when
        // then
        assertThatThrownBy(() -> underTest.getImage(image.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Image does not exist");
    }

    @Test
    void canGetProfileImage() {
        // given
        User user = mock(User.class);

        // when
        underTest.getProfileImage(user);

        // then
        verify(imageRepository).findByUserAndProfileImage(user, true);
    }

    @Test
    void canStoreCustomProfileImage() throws IOException {
        // given
        User user = mock(User.class);
        MultipartFile profileImage = mock(MultipartFile.class);

        given(profileImage.getOriginalFilename()).willReturn("path");


        // when
        underTest.storeProfileImage(user, profileImage);

        // then
        ArgumentCaptor<Image> imageArgumentCaptor = ArgumentCaptor.forClass(Image.class);

        verify(imageRepository).save(imageArgumentCaptor.capture());

        Image capturedImage = imageArgumentCaptor.getValue();

        assertEquals(user, capturedImage.getUser());
        assertEquals(profileImage.getOriginalFilename(), capturedImage.getName());
        assertEquals(profileImage.getContentType(), capturedImage.getType());
    }

    @Test
    void canStoreDefaultIndividualProfileImage() throws IOException {
        // given
        User user = mock(User.class);

        given(user.getUserRole()).willReturn(Role.INDIVIDUAL);

        // when
        underTest.storeProfileImage(user, null);

        // then
        ArgumentCaptor<Image> imageArgumentCaptor = ArgumentCaptor.forClass(Image.class);

        verify(imageRepository).save(imageArgumentCaptor.capture());

        Image capturedImage = imageArgumentCaptor.getValue();

        assertEquals(user, capturedImage.getUser());
        assertEquals("Individual-Illustration-1", capturedImage.getName());
        assertEquals("image/png", capturedImage.getType());
    }

    @Test
    void canStoreDefaultOrganizationProfileImage() throws IOException {
        // given
        User user = mock(User.class);

        given(user.getUserRole()).willReturn(Role.ORGANIZATION);

        // when
        underTest.storeProfileImage(user, null);

        // then
        ArgumentCaptor<Image> imageArgumentCaptor = ArgumentCaptor.forClass(Image.class);

        verify(imageRepository).save(imageArgumentCaptor.capture());

        Image capturedImage = imageArgumentCaptor.getValue();

        assertEquals(user, capturedImage.getUser());
        assertEquals("Organization-Illustration-1", capturedImage.getName());
        assertEquals("image/png", capturedImage.getType());
    }

    @Test
    void storeProfileImageWillThrowWhenNotAnUserRole() {
        // given
        //Image image = mock(Image.class);
        User user = mock(User.class);

        // this Role is not supported yet and makes our test work
        // because no image for admins
        given(user.getUserRole()).willReturn(Role.ADMIN);

        // when
        // then
        assertThatThrownBy(() -> underTest.storeProfileImage(user, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not an user role.");
    }

    @Test
    void canUpdateProfileImage() throws IOException {
        // given
        User user = mock(User.class);
        MultipartFile profileImage = mock(MultipartFile.class);

        given(profileImage.getOriginalFilename()).willReturn("path");

        // when
        underTest.updateProfileImage(user, profileImage);

        // then
        verify(imageRepository).delete(any());
        verify(imageRepository).save(any());
    }

    @Test
    void canStorePostImages() throws IOException {
        // given
        Post post = mock(Post.class);


        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);

        MultipartFile[] postImages = new MultipartFile[]{image1, image2};

        given(postImages[0].getOriginalFilename()).willReturn("path1");
        given(postImages[1].getOriginalFilename()).willReturn("path2");

        // when
        underTest.storePostImages(post, postImages);

        // then
        verify(imageRepository, times(2)).save(any());
    }

    @Test
    void canCreateFileDownloadUri() {
        // given
        Image image = mock(Image.class);
        Long imageId = 1L;

        given(image.getId()).willReturn(imageId);
        // when
        String actual = underTest.createFileDownloadUri(image);

        // then
        assertThat(actual).contains("/api/v1/images/image/" + imageId);
    }

    private Image addPostImage(User user, Post post, String imageData) {
        byte[] data = imageData.getBytes();
        return new Image(user, post,"name","type", data);
    }
}