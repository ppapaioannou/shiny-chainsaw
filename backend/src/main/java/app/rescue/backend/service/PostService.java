package app.rescue.backend.service;

import app.rescue.backend.payload.PostDto;
import app.rescue.backend.model.*;
import app.rescue.backend.payload.PostResponse;
import app.rescue.backend.repository.AnimalCharacteristicsRepository;
import app.rescue.backend.repository.ImageRepository;
import app.rescue.backend.repository.PostRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.util.LocationHelper;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final LocationHelper locationHelper;

    private final AnimalCharacteristicsRepository animalCharacteristicsRepository;



    public PostService(PostRepository postRepository, UserService userService,
                       UserRepository userRepository, ImageRepository imageRepository,
                       LocationHelper locationHelper, AnimalCharacteristicsRepository animalCharacteristicsRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.locationHelper = locationHelper;
        this.animalCharacteristicsRepository = animalCharacteristicsRepository;
    }

    public Post createNewAnimalPost(PostDto postDto, String postType) {
        //Post post = mapFromDtoToAnimalPost(postDto, postType);
        Post post = new Post();
        post.setAnimalCharacteristics(mapFromDtoToAnimalCharacteristics(postDto));

        postRepository.save(fillTheDetails(post, postDto, postType));
        for(Image postImage: post.getImages()) {
            postImage.setPost(post);
            imageRepository.save(postImage);
        }
        post.setImages(null); //dunno if this is needed
        return post;
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

    public List<PostResponse> getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort;
        if (sortDir.equalsIgnoreCase("asc")) {
            sort = Sort.by(sortBy).ascending();
        }
        else if (sortDir.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        }
        else {
            throw new IllegalStateException("unknown sorting method");
        }

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> posts = postRepository.findAll(pageable);

        return posts.stream().map(this::mapFromPostToResponse).collect(Collectors.toList());
    }

    public List<PostResponse> findAll(Specification<Post> specs) {
        List<Post> posts = postRepository.findAll(specs);
        return posts.stream().map(this::mapFromPostToResponse).collect(Collectors.toList());
        //return null;
    }

    private PostResponse mapFromPostToResponse(Post post) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setUsername(post.getUser().getName());
        postResponse.setTitle(post.getTitle());
        postResponse.setBody(post.getBody());
        postResponse.setPostType(post.getPostType());

        if (postResponse.getPostType().equals("missing")) {
            postResponse.setAnimalType(post.getAnimalCharacteristics().getAnimalType());
            postResponse.setBreed(post.getAnimalCharacteristics().getBreed());
            postResponse.setColor(post.getAnimalCharacteristics().getColor().split(","));
            postResponse.setGender(post.getAnimalCharacteristics().getGender());
            postResponse.setSize(post.getAnimalCharacteristics().getSize());
            //postResponse.setMissingDate(((MissingPost) post).getMissingDate().toString());
        }
        postResponse.setPostStatus(post.getPostStatus());

        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        postResponse.setCreatedAt(post.getCreatedAt().format(dateTimeFormatter));
        postResponse.setEnableComments(post.getEnableComments());
        //postResponse.setImages(post.getImages()); TODO convert image data to something that can be viewed
        //postResponse.setLocation(post.getLocation()); TODO convert lat,long to address - Geo mapper?
        Geometry postLocation = post.getLocation();
        Geometry userLocation = userRepository.findUserByEmail(userService.getCurrentUser()).getLocation();
        double distance = locationHelper.getDistanceFromPostInMeters(userLocation, postLocation);
        if (distance == -1.0) {
            postResponse.setDistance("n/a");
            //postRepository.setPostDistance(post.getId(), Double.MAX_VALUE);
        }
        else {
            postResponse.setDistance(String.format("%.2f", distance) + " meters");
            //post.setDistance(distance);
            //postRepository.setPostDistance(post.getId(), distance);
        }
        //postRepository.setPostDistance(post.getId(), null);

        //postResponse.setAnimalType(post.getAnimalType());

        return postResponse;

    }

    private AnimalCharacteristics mapFromDtoToAnimalCharacteristics(PostDto postDto) {
        AnimalCharacteristics animalCharacteristics = new AnimalCharacteristics();
        animalCharacteristics.setAnimalType(postDto.getAnimalType());
        animalCharacteristics.setBreed(postDto.getBreed());
        //animalCharacteristics.setColor(Arrays.toString(postDto.getColors()));
        animalCharacteristics.setColor(String.join(",",postDto.getColors()));
        animalCharacteristics.setGender(postDto.getGender());
        animalCharacteristics.setSize(postDto.getSize());
        animalCharacteristicsRepository.save(animalCharacteristics);
        return animalCharacteristics;
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

        if (postDto.getLatitude() != null && postDto.getLongitude() != null) {
            Geometry postLocation = locationHelper.postLocationToPoint(Double.parseDouble(postDto.getLatitude()),
                    Double.parseDouble(postDto.getLongitude()));
            post.setLocation(postLocation);
        }


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
