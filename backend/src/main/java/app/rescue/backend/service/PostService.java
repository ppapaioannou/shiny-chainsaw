package app.rescue.backend.service;

import app.rescue.backend.dto.PostDto;
import app.rescue.backend.model.*;
import app.rescue.backend.repository.ImageRepository;
import app.rescue.backend.repository.PostRepository;
import app.rescue.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public PostService(PostRepository postRepository, UserService userService, UserRepository userRepository, ImageRepository imageRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    /*
    public List<PostDto> showAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::mapFromPostToDto).collect(Collectors.toList());
    }
    */

    /*
    public void createPost(PostDto postDto) {
        Post post = mapFromDtoToPost(postDto);
        postRepository.save(post);
        for(Image postImage: post.getImages()) {
            postImage.setPost(post);
            imageRepository.save(postImage);
        }
        post.setImages(null);
    }
    */

    public void createNewAnimalPost(PostDto postDto, String postType) {
        Post post = mapFromDtoToAnimalPost(postDto, postType);

        postRepository.save(fillTheDetails(post, postDto, postType));
        for(Image postImage: post.getImages()) {
            postImage.setPost(post);
            imageRepository.save(postImage);
        }
        post.setImages(null);
    }

    public void createNewSimplePost(PostDto postDto) {
        Post post = mapFromDtoToSimplePost(postDto);

        postRepository.save(fillTheDetails(post, postDto, "simple"));
        for(Image postImage: post.getImages()) {
            postImage.setPost(post);
            imageRepository.save(postImage);
        }
        post.setImages(null);
    }

    public void createNewEventPost(PostDto postDto) {
        Post post = mapFromDtoToEventPost(postDto);

        postRepository.save(fillTheDetails(post, postDto, "event"));
        for(Image postImage: post.getImages()) {
            postImage.setPost(post);
            imageRepository.save(postImage);
        }
        post.setImages(null);
    }

    private Post fillTheDetails(Post post, PostDto postDto, String postType) {
        //TODO create ImageService class to handle all image operations
        List<Image> postImages = getPostImages(postDto);
        post.setTitle(postDto.getTitle());
        post.setImages(postImages);
        post.setBody(postDto.getBody());
        post.setPostType(postType);
        post.setPostStatus(postDto.getPostStatus());
        post.setEnableComments(postDto.getEnableComments());
        post.setCreatedAt(LocalDateTime.now());
        post.setUser(userRepository.findUserByEmail(userService.getCurrentUser()));
        return post;
    }

    private ArrayList<Image> getPostImages(PostDto postDto) {
        ArrayList<Image> postImages = new ArrayList<>();
        for (Byte[] postImageData :postDto.getImagesData()) {
            Image image = new Image();
            image.setImageType("postImage");
            image.setData(postImageData);
            postImages.add(image);
        }
        return postImages;
    }

    /*
    public PostDto readSinglePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("For id " + id));
        return mapFromPostToDto(post);
    }
    */


    /*
    private PostDto mapFromPostToDto(Post post) {
        List<Image> postImages = imageRepository.findAllImagesByPostId(post.getId());
        List<Byte[]> postImagesDto = new ArrayList<Byte[]>();
        for (Image image: postImages) {
            postImagesDto.add(image.getData());
        }
        PostDto postDto = new PostDto(post.getId(), post.getTitle(), postImagesDto, post.getBody(),
                post.getPostType(), post.getPostStatus(), post.getUser().getId());
        return postDto;
    }
    */


    private Post mapFromDtoToAnimalPost(PostDto postDto, String postType) {
        AnimalPost post;
        switch (postType) {
            case "missing":
                post = new MissingPost();
                ((MissingPost) post).setMissingDate(Date.valueOf(postDto.getMissingDate()));
                ((MissingPost) post).setMissingMicrochipNumber(postDto.getMissingMicrochipNumber());
                break;
            case "adoption":
                post = new AdoptionPost();
                ((AdoptionPost) post).setAge(postDto.getAge());
                ((AdoptionPost) post).setNeutered(postDto.getNeutered());
                ((AdoptionPost) post).setAdoptionMicrochipNumber(postDto.getAdoptionMicrochipNumber());
                ((AdoptionPost) post).setGoodWithChildren(postDto.getGoodWithChildren());
                ((AdoptionPost) post).setGoodWithAnimals(postDto.getGoodWithAnimals());
                break;
            case "stray":
                post = new StrayPost();
                ((StrayPost) post).setStrayDate(Date.valueOf(postDto.getStrayDate()));
                ((StrayPost) post).setActionsTaken(postDto.getActionsTaken());
                break;
            default:
                throw new IllegalStateException("unknown postType");
        }
        post.setAnimalType(postDto.getAnimalType());
        //post.setAnimalLocation(postDto.getAnimalLocation());
        post.setBreed(postDto.getBreed());
        post.setGender(postDto.getGender());
        post.setColor(postDto.getColor());
        post.setSize(postDto.getSize());

        return post;

    }

    private Post mapFromDtoToSimplePost(PostDto postDto) {
        SimplePost post = new SimplePost();
        //post.setEnableDiscussion(postDto.getEnableDiscussion());
        return post;
    }

    private Post mapFromDtoToEventPost(PostDto postDto) {
        EventPost post = new EventPost();
        post.setEventAddress(postDto.getEventAddress());
        //post.setEventLocation(postDto.getEventLocation());
        post.setEventDate(Date.valueOf(postDto.getEventDate()));
        post.setEventTime(Time.valueOf(postDto.getEventTime()));
        //post.setEnableEventDiscussion(postDto.getEnableEventDiscussion());
        return post;
    }

}
