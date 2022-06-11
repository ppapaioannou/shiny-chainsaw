package app.rescue.backend.controller;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.*;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConnectionRepository connectionRepository;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void canRegister() throws Exception {
        // given
        RegistrationDto request = getRegistrationDto();
        String userRole = Role.INDIVIDUAL.toString();

        // when
        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        assertThat(userRepository.findByEmail(request.getEmail())).isPresent();

        User user = userService.getUserByEmail(request.getEmail());

        assertEquals(request.getName(), user.getName());
        assertEquals(userRole, user.getUserRole().toString());

        assertThat(imageService.getProfileImage(user).getName()).isEqualTo("Individual-Illustration-1");
    }

    @Test
    void canRefRegister() throws Exception {
        // given
        User existingUser = getUser();
        RegistrationDto request = getRegistrationDto();
        String refToken = existingUser.getReferralToken();
        String userRole = Role.INDIVIDUAL.toString();

        // when
        mvc.perform(post("/api/v1/auth/ref/register/" + refToken + "/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        assertThat(userRepository.findByEmail(request.getEmail())).isPresent();
        //assertThat(userRepository.existsByEmail(request.getEmail())).isTrue();

        User user = userService.getUserByEmail(request.getEmail());

        assertEquals(request.getName(), user.getName());
        assertEquals(userRole, user.getUserRole().toString());

        List<Connection> connections = connectionRepository.findConnectionsByUser(existingUser);
        assertThat(connections.get(0).getConnectedToId()).isEqualTo(user.getId());
        assertThat(connections.get(0).getConnectionStatus()).isEqualTo("REF-PENDING");

        List<NotificationDto> notifications = notificationService.getAllNotifications(existingUser.getEmail());
        assertThat(notifications.get(0).getSender()).isEqualTo(user.getName());
        assertThat(notifications.get(0).getText()).contains("Your friend created a Rescue account, " +
                "when they enable their account you two will be automatically connected");
    }

    @Test
    void canLogin() throws Exception {
        // given
        RegistrationDto registrationRequest = getRegistrationDto();
        String userRole = Role.INDIVIDUAL.toString();
        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        User expected = userService.getUserByEmail(registrationRequest.getEmail());
        userService.enableUser(expected.getEmail());
        LoginDto request = getLoginDto(expected.getEmail());

        // when
        MvcResult getAuthenticationDto = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        String contentAsString = getAuthenticationDto
                .getResponse()
                .getContentAsString();

        AuthenticationDto actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    @Test
    void canConfirm() throws Exception {
        // given
        RegistrationDto registrationRequest = getRegistrationDto();
        String userRole = Role.INDIVIDUAL.toString();
        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        User user = userService.getUserByEmail(registrationRequest.getEmail());
        String token = user.getConfirmationTokens().iterator().next().getToken();

        // when
        MvcResult getAuthenticationDto = mvc.perform(get("/api/v1/auth/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("token", token))
                .andExpect(status().isOk())
                .andReturn();

        //then
        String actual = getAuthenticationDto
                .getResponse()
                .getContentAsString();

        assertThat(actual).isEqualTo("confirmed");
    }

    private RegistrationDto getRegistrationDto() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("new-user@example.com");
        registrationDto.setName("name");
        registrationDto.setPassword("password");

        return registrationDto;

    }

    private LoginDto getLoginDto(String email) {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword("password");

        return loginDto;
    }

    private User getUser() {
        User user = new User();
        user.setEmail("existing-user@example.com");
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.INDIVIDUAL);
        userRepository.save(user);
        return user;
    }

}