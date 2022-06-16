package app.rescue.backend.service;

import app.rescue.backend.payload.PostDto;
import app.rescue.backend.model.*;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.repository.AnimalCharacteristicsRepository;
import app.rescue.backend.repository.EventPropertiesRepository;
import app.rescue.backend.repository.PostRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
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
    private final ImageService imageService;

    public PostService(PostRepository postRepository, AnimalCharacteristicsRepository animalCharacteristicsRepository,
                       EventPropertiesRepository eventPropertiesRepository, UserService userService,
                       LocationService locationService, ImageService imageService) {
        this.postRepository = postRepository;
        this.animalCharacteristicsRepository = animalCharacteristicsRepository;
        this.eventPropertiesRepository = eventPropertiesRepository;
        this.userService = userService;
        this.locationService = locationService;
        this.imageService = imageService;
    }


    public Post createNewPost(PostDto request, String postType, String username) {
        if (!postTypes.contains(postType)) {
            throw new IllegalStateException(String.format("%s posts not allowed", postType));
        }
        Post post = mapFromRequestToPost(request);
        post.setPostType(postType);
        post.setUser(userService.getUserByEmail(username));
        postRepository.save(post);

        if (postType.equals("missing") || postType.equals("adoption") || postType.equals("stray")) {
            post.setAnimalCharacteristics(mapFromRequestToAnimalCharacteristics(request, post));
        }
        else if (postType.equals("event")) {
            post.setEventProperties(mapFromRequestToEventProperties(request, post));
        }
        return post;
    }

    public List<PostDto> getAllPosts(Specification<Post> specs, String username) {
        Sort sort = Sort.by("id").descending();
        List<Post> posts = postRepository.findAll(specs, sort);

        return posts.stream().map(post -> mapFromPostToResponse(post, username)).collect(Collectors.toList());
    }

    public PostDto getSinglePost(Long postId, String username) {
        Post post = findById(postId);
        return mapFromPostToResponse(post, username);
    }

    //TODO public void editPost()

    public void attendEvent(Long postId, String username) {
        Post post = findById(postId);
        if (!post.getPostType().equals("event")) {
            throw new IllegalStateException("This is not an event");
        }
        User user = userService.getUserByEmail(username);

        if (eventPropertiesRepository.existsByPostAndEventAttendeesContaining(post, user)) {
            post.getEventProperties().removeEventAttendee(user);
        }
        else {
            post.getEventProperties().addEventAttendee(user);
        }
        postRepository.save(post);

    }

    public void deletePost(Long postId, String username) {
        Post post = findById(postId);
        if (post.getUser().getEmail().equals(username)) {
            postRepository.delete(post);
        }
        else {
            throw new IllegalStateException("You don't have permission to delete this post");
        }
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalStateException(String.format("Post with ID:%s does not exist", id)));
    }

    public void addCommentator(Post post, User user) {
        if (post.addCommentator(user)) {
            postRepository.save(post);
        }
    }


    private Post mapFromRequestToPost(PostDto request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        if (request.getDate() != null) {
            post.setDate(Date.valueOf(request.getDate()));
        }
        if (!request.getLatitude().isEmpty() && !request.getLongitude().isEmpty()) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            Geometry postLocation = locationService.turnPostLocationToPoint(latitude, longitude);
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
        animalCharacteristics.setAge(postDto.getAge());
        if (post.getPostType().equals("adoption")) {
            animalCharacteristics.setNeutered(turnStringToBoolean(postDto.getNeutered()));
            animalCharacteristics.setGoodWithAnimals(turnStringToBoolean(postDto.getGoodWithAnimals()));
            animalCharacteristics.setGoodWithChildren(turnStringToBoolean(postDto.getGoodWithChildren()));
        }
        animalCharacteristics.setActionsTaken(postDto.getActionTaken());
        animalCharacteristicsRepository.save(animalCharacteristics);
        return animalCharacteristics;
    }

    private EventProperties mapFromRequestToEventProperties(PostDto postDto, Post post) {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setPost(post);
        eventProperties.setTime(Time.valueOf(postDto.getTime()));
        eventPropertiesRepository.save(eventProperties);
        return eventProperties;
    }

    private PostDto mapFromPostToResponse(Post post, String username) {
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        PostDto postResponse = new PostDto();
        postResponse.setId(post.getId());
        String userName = post.getUser().getName();
        if (post.getUser().getIndividualInformation() != null) {
            userName += " " + post.getUser().getIndividualInformation().getLastName();
        }
        postResponse.setUserName(userName);
        postResponse.setUserId(post.getUser().getId().toString());
        postResponse.setTitle(post.getTitle());
        postResponse.setBody(post.getBody());
        postResponse.setDate(post.getDate().toLocalDate().format(dateFormatter));
        postResponse.setPostType(post.getPostType());
        postResponse.setNumberOfComments(post.getComments().size());

        //TODO maybe different methods for these
        if (post.getAnimalCharacteristics() != null) {
            postResponse.setAnimalType(post.getAnimalCharacteristics().getAnimalType());
            postResponse.setBreed(post.getAnimalCharacteristics().getBreed());
            postResponse.setGender(post.getAnimalCharacteristics().getGender());
            postResponse.setSize(post.getAnimalCharacteristics().getSize());
            postResponse.setColors(post.getAnimalCharacteristics().getColor().split(","));
            postResponse.setAge(post.getAnimalCharacteristics().getAge());
            postResponse.setMicrochipNumber(post.getAnimalCharacteristics().getMicrochipNumber());
            if (post.getPostType().equals("adoption")) {
                postResponse.setNeutered(turnBooleanToString(post.getAnimalCharacteristics().getNeutered()));
                postResponse.setGoodWithAnimals(turnBooleanToString(post.getAnimalCharacteristics().getGoodWithAnimals()));
                postResponse.setGoodWithChildren(turnBooleanToString(post.getAnimalCharacteristics().getGoodWithChildren()));
            }
            postResponse.setActionTaken(post.getAnimalCharacteristics().getActionsTaken());
        }
        if (post.getEventProperties() != null) {
            String time = post.getEventProperties().getTime().toString();
            postResponse.setTime(time.substring(0,time.length()-3));
            List<UserDto> eventAttendees = post.getEventProperties().getEventAttendees().stream().map(userService::mapFromUserToResponse).collect(Collectors.toList());
            postResponse.setEventAttendees(eventAttendees);
        }
        postResponse.setCreatedAt(post.getCreatedAt().format(timestampFormatter));

        Collection<Image> postImages = post.getImages();
        if (postImages.iterator().hasNext()) {
            Image image = postImages.iterator().next();
            postResponse.setThumbnail(imageService.createFileDownloadUri(image));
        }

        postResponse.setAddress(post.getAddress());
        if (!username.isEmpty()) {
            User user = userService.getUserByEmail(username);
            double distance = locationService.getDistanceFromPostInMeters(user.getLocation(), post.getLocation());
            if (distance != -1.0) {
                String test = String.format("%.2f", distance);
                postResponse.setDistance(Double.valueOf(test));

                // post tha are too far away will have '0' as id and the frontend will not display them
                if (!locationService.proximityCheck(user.getLocation(), post.getLocation())) {
                    postResponse.setId(0L);
                }
            }
        }
        return postResponse;
    }

    private String turnBooleanToString(Boolean bool) {
        if (bool) {
            return "Yes";
        }
        else {
            return "No";
        }
    }

    private Boolean turnStringToBoolean(String s) {
        return s.equals("Yes");
    }

}
