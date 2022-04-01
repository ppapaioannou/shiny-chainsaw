package app.rescue.backend.service;

import app.rescue.backend.payload.request.PostRequest;
import app.rescue.backend.model.*;
import app.rescue.backend.payload.resposne.PostResponse;
import app.rescue.backend.repository.AnimalCharacteristicsRepository;
import app.rescue.backend.repository.EventPropertiesRepository;
import app.rescue.backend.repository.PostRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final List<String> postTypes = List.of("missing", "adoption", "stray", "simple", "event");

    private final PostRepository postRepository;
    private final AnimalCharacteristicsRepository animalCharacteristicsRepository;
    private final EventPropertiesRepository eventPropertiesRepository;

    private final UserService userService;
    private final LocationService locationService;

    public PostService(PostRepository postRepository, AnimalCharacteristicsRepository animalCharacteristicsRepository,
                       EventPropertiesRepository eventPropertiesRepository, UserService userService,
                       LocationService locationService) {
        this.postRepository = postRepository;
        this.animalCharacteristicsRepository = animalCharacteristicsRepository;
        this.eventPropertiesRepository = eventPropertiesRepository;
        this.userService = userService;
        this.locationService = locationService;
    }


    public Post createNewPost(PostRequest request, String postType, String userName) {
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

    public List<PostResponse> getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir, Specification<Post> specs) {
        /*
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
        */
        List<Post> posts = postRepository.findAll(specs);
        return posts.stream().map(this::mapFromPostToResponse).collect(Collectors.toList());
    }

    public PostResponse getSinglePost(Long postId) {
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


    private Post mapFromRequestToPost(PostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setImages(request.getImagesData());
        post.setBody(request.getBody());
        post.setEnableComments(request.getEnableComments());
        if (request.getDate() != null) {
            post.setDate(Date.valueOf(request.getDate()));
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            Geometry postLocation = locationService.postLocationToPoint(latitude, longitude);
            post.setLocation(postLocation);
        }
        return  post;
    }

    private AnimalCharacteristics mapFromRequestToAnimalCharacteristics(PostRequest postDto, Post post) {
        AnimalCharacteristics animalCharacteristics = new AnimalCharacteristics();
        animalCharacteristics.setPost(post);
        animalCharacteristics.setAnimalType(postDto.getAnimalType());
        animalCharacteristics.setBreed(postDto.getBreed());
        animalCharacteristics.setColor(String.join(",",postDto.getColors()));
        animalCharacteristics.setGender(postDto.getGender());
        animalCharacteristics.setSize(postDto.getSize());
        animalCharacteristicsRepository.save(animalCharacteristics);
        return animalCharacteristics;
    }

    private EventProperties mapFromRequestToEventProperties(PostRequest postDto, Post post) {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setPost(post);
        eventProperties.setAddress(postDto.getAddress());
        eventProperties.setTime(Time.valueOf(postDto.getTime()));
        eventPropertiesRepository.save(eventProperties);
        return eventProperties;
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

        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        postResponse.setCreatedAt(post.getCreatedAt().format(dateTimeFormatter));
        postResponse.setDate(post.getDate().toString());
        postResponse.setEnableComments(post.getEnableComments());
        //postResponse.setImages(post.getImages()); TODO convert image data to something that can be viewed
        //postResponse.setLocation(post.getLocation()); TODO convert lat,long to address - Geo mapper?
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
