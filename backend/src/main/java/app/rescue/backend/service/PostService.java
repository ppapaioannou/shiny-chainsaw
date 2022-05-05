package app.rescue.backend.service;

import app.rescue.backend.controller.ImageController;
import app.rescue.backend.payload.PostDto;
import app.rescue.backend.model.*;
import app.rescue.backend.repository.AnimalCharacteristicsRepository;
import app.rescue.backend.repository.EventPropertiesRepository;
import app.rescue.backend.repository.PostRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.coyote.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.sql.Date;
import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final List<String> postTypes = List.of("missing", "adoption", "stray", "simple", "event");

    private final PostRepository postRepository;
    private final AnimalCharacteristicsRepository animalCharacteristicsRepository;
    private final EventPropertiesRepository eventPropertiesRepository;

    private final UserService userService;
    private final LocationService locationService;
    //private final ImageService imageService;

    //private final ImageController imageController;

    public PostService(PostRepository postRepository, AnimalCharacteristicsRepository animalCharacteristicsRepository,
                       EventPropertiesRepository eventPropertiesRepository, UserService userService,
                       LocationService locationService) {
        this.postRepository = postRepository;
        this.animalCharacteristicsRepository = animalCharacteristicsRepository;
        this.eventPropertiesRepository = eventPropertiesRepository;
        this.userService = userService;
        this.locationService = locationService;
    }


    public Post createNewPost(PostDto request, String postType, String userName) {
        if (!postTypes.contains(postType)) {
            throw new IllegalStateException(String.format("%s posts not allowed", postType));
        }
        Post post = mapFromRequestToPost(request);
        post.setPostType(postType);
        post.setUser(userService.getUserByEmail(userName));
        //return post;
        postRepository.save(post);

        if (postType.equals("missing") || postType.equals("adoption") || postType.equals("stray")) {
            post.setAnimalCharacteristics(mapFromRequestToAnimalCharacteristics(request, post));
        }
        else if (postType.equals("event")) {
            post.setEventProperties(mapFromRequestToEventProperties(request, post));
        }


        /*
        switch (postType) {
            case "missing":
            case "adoption":
            case "stray":
                post.setAnimalCharacteristics(mapFromDtoToAnimalCharacteristics(request));
                break;
            case "simple":
                break;
            case "event":
                post.setEventProperties(mapFromDtoToEventProperties(request));
                break;
            default:
                throw new IllegalStateException(String.format("%s posts not allowed", postType));
        }
        */
        return post;
    }

    public List<PostDto> getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir, Specification<Post> specs) {

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

        //Page<Post> posts = postRepository.findAll(pageable);

        List<Post> posts = postRepository.findAll(specs);
        return posts.stream().map(this::mapFromPostToResponse).collect(Collectors.toList());
    }

    public PostDto getSinglePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new IllegalStateException(String.format("Post not found for ID:%s",postId)));
        return mapFromPostToResponse(post);
    }

    public void updatePost(Long postId, String userName) {
    }

    public void willAttendEvent(Long postId, String userName) {
        Post post = findById(postId);
        if (!post.getPostType().equals("event")) {
            throw new IllegalStateException("This is not an event");
        }
        User user = userService.getUserByEmail(userName);

        if (post.getEventProperties().getEventAttendees().contains(user)) {
            throw new IllegalStateException("Already answered");
        }
        post.getEventProperties().addEventAttendee(user);
        postRepository.save(post);
    }

    public void deletePost(Long postId, String userName) {
        //Post post = postRepository.findById(postId).orElseThrow(() ->
        //        new IllegalStateException(String.format("Post not found for ID:%s", postId)));
        Post post = findById(postId);

        if (post.getUser().getEmail().equals(userName)) {
            postRepository.delete(post);
        }
        else {
            throw new IllegalStateException("You don't have permission to delete this post");
        }
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("Post does not exits"));
    }

    public void addCommentator(Post post, User user) {
        post.addCommentator(user);
        postRepository.save(post);
    }


    private Post mapFromRequestToPost(PostDto request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        //post.setImages(request.getImagesData());
        post.setBody(request.getBody());
        if (request.getEnableComments() != null) {
            post.setEnableComments(request.getEnableComments());
        }
        if (!Objects.equals(request.getDate(), "")) {
            post.setDate(Date.valueOf(request.getDate()));
        }
        if (!request.getLatitude().isEmpty() && !request.getLongitude().isEmpty()) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            Geometry postLocation = locationService.postLocationToPoint(latitude, longitude);
            post.setLocation(postLocation);

            post.setAddress(request.getAddress());
        }
        return  post;
    }

    private AnimalCharacteristics mapFromRequestToAnimalCharacteristics(PostDto postDto, Post post) {
        AnimalCharacteristics animalCharacteristics = new AnimalCharacteristics();
        animalCharacteristics.setPost(post);
        animalCharacteristics.setAnimalType(postDto.getAnimalType());
        animalCharacteristics.setBreed(postDto.getBreed());
        animalCharacteristics.setColor(String.join(",",postDto.getColors()));
        animalCharacteristics.setGender(postDto.getGender());
        animalCharacteristics.setSize(postDto.getSize());
        animalCharacteristics.setMicrochipNumber(postDto.getMicrochipNumber());
        animalCharacteristicsRepository.save(animalCharacteristics);
        return animalCharacteristics;
    }

    private EventProperties mapFromRequestToEventProperties(PostDto postDto, Post post) {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setPost(post);
        //eventProperties.setAddress(postDto.getAddress());
        eventProperties.setTime(Time.valueOf(postDto.getTime()));
        eventPropertiesRepository.save(eventProperties);
        return eventProperties;
    }

    private PostDto mapFromPostToResponse(Post post) {
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        PostDto postResponse = new PostDto();
        postResponse.setId(post.getId());
        postResponse.setUsername(post.getUser().getName() + " " + post.getUser().getIndividualInformation().getLastName());
        postResponse.setTitle(post.getTitle());
        postResponse.setBody(post.getBody());
        postResponse.setPostType(post.getPostType());


        if (postResponse.getPostType().equals("missing")) {
            postResponse.setAnimalType(post.getAnimalCharacteristics().getAnimalType());
            postResponse.setBreed(post.getAnimalCharacteristics().getBreed());
            postResponse.setColors(post.getAnimalCharacteristics().getColor().split(","));
            postResponse.setGender(post.getAnimalCharacteristics().getGender());
            postResponse.setSize(post.getAnimalCharacteristics().getSize());
            postResponse.setMicrochipNumber(post.getAnimalCharacteristics().getMicrochipNumber());
            postResponse.setDate(post.getDate().toLocalDate().format(dateFormatter));
        }
        postResponse.setCreatedAt(post.getCreatedAt().format(timestampFormatter));
        postResponse.setEnableComments(post.getEnableComments());
        Collection<Image> postImages = post.getImages();
        if (postImages.iterator().hasNext()) {
            Long imageId = postImages.iterator().next().getId();
            String thumbnailLink = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/v1/images/image/")
                    .path(String.valueOf(imageId))
                    .toUriString();
            postResponse.setThumbnail(thumbnailLink);
        }
        //postResponse.setThumbnail(imageService.getPostThumbnail(post));
        //postResponse.setThumbnail(imageService.getImage());
        //System.out.println(imageService.);
        //ResponseEntity<byte[]> request = imageController.getImage(imageService.getPostThumbnail(post).getId());
        //request.getD
        //System.out.println(imageController.getImage(imageService.getPostThumbnail(post).getId()).);

        //postResponse.setImagesData(post.getImages().stream());
        //postResponse.setImages(post.getImages()); TODO convert image data to something that can be viewed
        //postResponse.setLocation(post.getLocation()); TODO convert lat,long to address - Geo mapper?
        postResponse.setAddress(post.getAddress());
        double distance = locationService.getDistanceFromPostInMeters(post.getUser().getLocation(), post.getLocation());

        if (distance == -1.0) {
            postResponse.setDistance("n/a");
        } else {
            postResponse.setDistance(String.format("%.2f", distance) + " meters");
        }
        //postRepository.setPostDistance(post.getId(), null);

        //postResponse.setAnimalType(post.getAnimalType());

        return postResponse;

    }
}
