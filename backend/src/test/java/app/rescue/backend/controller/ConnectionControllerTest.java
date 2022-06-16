package app.rescue.backend.controller;

import app.rescue.backend.model.Connection;
import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.*;
import app.rescue.backend.repository.ConnectionRepository;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.service.ConnectionService;
import app.rescue.backend.service.NotificationService;
import app.rescue.backend.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class ConnectionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConnectionRepository connectionRepository;

    private User individual;
    private User otherIndividual;
    private User otherOtherIndividual;
    private User organization;
    private User otherOrganization;

    private Principal individualMockPrincipal;
    private Principal otherIndividualMockPrincipal;
    private Principal otherOtherIndividualMockPrincipal;
    private Principal organizationMockPrincipal;

    @BeforeAll
    void setUp() throws Exception {
        RegistrationDto request = getRegistrationDto("individual@example.com");
        String userRole = Role.INDIVIDUAL.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        individual = userService.getUserByEmail(request.getEmail());

        request = getRegistrationDto("other-individual@example.com");
        userRole = Role.INDIVIDUAL.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        otherIndividual = userService.getUserByEmail(request.getEmail());

        request = getRegistrationDto("other-other-individual@example.com");
        userRole = Role.INDIVIDUAL.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        otherOtherIndividual = userService.getUserByEmail(request.getEmail());

        request = getRegistrationDto("organization@example.com");
        userRole = Role.ORGANIZATION.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        organization = userService.getUserByEmail(request.getEmail());

        request = getRegistrationDto("otherOrganization@example.com");
        userRole = Role.ORGANIZATION.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        otherOrganization = userService.getUserByEmail(request.getEmail());

        individualMockPrincipal = mock(Principal.class);
        given(individualMockPrincipal.getName()).willReturn(individual.getEmail());

        otherIndividualMockPrincipal = mock(Principal.class);
        given(otherIndividualMockPrincipal.getName()).willReturn(otherIndividual.getEmail());

        otherOtherIndividualMockPrincipal = mock(Principal.class);
        given(otherOtherIndividualMockPrincipal.getName()).willReturn(otherOtherIndividual.getEmail());

        organizationMockPrincipal = mock(Principal.class);
        given(organizationMockPrincipal.getName()).willReturn(organization.getEmail());

        Principal otherOrganizationMockPrincipal = mock(Principal.class);
        given(otherOrganizationMockPrincipal.getName()).willReturn(otherOrganization.getEmail());
    }

    @AfterEach
    void resetConnectionRepository() {
        connectionRepository.deleteAll();
    }

    @AfterAll
    void tearDown() {
        connectionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canConnectWith() throws Exception {
        // given
        // when
        mvc.perform(post("/api/v1/connection/with/" + otherIndividual.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(individualMockPrincipal))
                .andExpect(status().isOk());

        // then
        List<ConnectionDto> connections = connectionService.getAllConnections("friend-requests", otherIndividual.getEmail());
        assertEquals(connections.get(0).getUserId(), individual.getId());
        assertEquals(connections.get(0).getName(), individual.getName());


        List<NotificationDto> notifications = notificationService.getAllNotifications(otherIndividual.getEmail());
        assertThat(notifications.get(0).getSender()).isEqualTo(individual.getName());
        assertThat(notifications.get(0).getText()).contains("New Friend Request");
    }

    @Test
    void canGetAllFriendRequestConnections() throws Exception{
        //given
        List<Connection> expected = new ArrayList<>();
        expected.add(getConnection(individual, otherOtherIndividual, "PENDING"));
        expected.add(getConnection(otherIndividual, otherOtherIndividual, "PENDING"));
        String connectionType = "friend-requests";

        //when
        MvcResult getConnectionsResult = mvc.perform(get("/api/v1/connection/" + connectionType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(otherOtherIndividualMockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getConnectionsResult
                .getResponse()
                .getContentAsString();

        List<ConnectionDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getUser().getId(), actual.get(i).getUserId());
            assertEquals(expected.get(i).getUser().getName(), actual.get(i).getName());
        }
    }

    @Test
    void canGetAllFriendsConnections() throws Exception{
        //given
        List<Connection> expected = new ArrayList<>();
        expected.add(getConnection(otherOtherIndividual, individual, "CONNECTED"));
        expected.add(getConnection(otherOtherIndividual, otherIndividual, "CONNECTED"));
        String connectionType = "friends";

        //when
        MvcResult getConnectionsResult = mvc.perform(get("/api/v1/connection/" + connectionType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(otherOtherIndividualMockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getConnectionsResult
                .getResponse()
                .getContentAsString();

        List<ConnectionDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getConnectedToId(), actual.get(i).getUserId());
            assertEquals(expected.get(i).getUser().getName(), actual.get(i).getName());
        }
    }

    @Test
    void canGetAllFollowersConnections() throws Exception{
        //given
        List<Connection> expected = new ArrayList<>();
        expected.add(getConnection(individual, organization, "FOLLOWER"));
        expected.add(getConnection(otherIndividual, organization, "FOLLOWER"));
        String connectionType = "followers";

        //when
        MvcResult getConnectionsResult = mvc.perform(get("/api/v1/connection/" + connectionType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(organizationMockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getConnectionsResult
                .getResponse()
                .getContentAsString();

        List<ConnectionDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getUser().getId(), actual.get(i).getUserId());
            assertEquals(expected.get(i).getUser().getName(), actual.get(i).getName());
        }
    }

    @Test
    void canGetAllOrganizationsConnections() throws Exception{
        //given
        List<Connection> expected = new ArrayList<>();
        expected.add(getConnection(individual, organization, "FOLLOWER"));
        expected.add(getConnection(individual, otherOrganization, "FOLLOWER"));
        String connectionType = "organizations";

        //when
        MvcResult getConnectionsResult = mvc.perform(get("/api/v1/connection/" + connectionType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(individualMockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getConnectionsResult
                .getResponse()
                .getContentAsString();

        List<ConnectionDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getConnectedToId(), actual.get(i).getUserId());
            assertEquals(expected.get(i).getUser().getName(), actual.get(i).getName());
        }
    }

    @Test
    void canGetConnectionStatus() throws Exception {
        //given
        getConnection(individual, otherIndividual, "CONNECTED");

        //when
        MvcResult getConnectionsResult = mvc.perform(get("/api/v1/connection/status/" + otherIndividual.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(individualMockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getConnectionsResult
                .getResponse()
                .getContentAsString();

        assertThat(contentAsString).isEqualTo("CONNECTED");
    }

    @Test
    void canAcceptConnection() throws Exception {
        //given
        getConnection(individual, otherIndividual, "PENDING");

        assertThat(connectionRepository.findPendingConnection(individual, otherIndividual.getId())).isPresent();
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(otherIndividual, individual.getId())).isNotPresent();

        //when
        mvc.perform(put("/api/v1/connection/accept/" + individual.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(otherIndividualMockPrincipal))
                .andExpect(status().isOk());

        // then
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(individual, otherIndividual.getId())).isPresent();
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(otherIndividual, individual.getId())).isPresent();
    }

    @Test
    void canDeclineConnection() throws Exception {
        //given
        getConnection(otherIndividual, individual, "PENDING");

        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(otherIndividual, individual.getId())).isPresent();

        //when
        mvc.perform(delete("/api/v1/connection/decline/" + otherIndividual.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(individualMockPrincipal))
                .andExpect(status().isOk());

        // then
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(individual, otherIndividual.getId())).isNotPresent();
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(otherIndividual, individual.getId())).isNotPresent();
    }

    @Test
    void canDeleteConnection() throws Exception {
        //given
        getConnection(individual, otherIndividual, "CONNECTED");
        getConnection(otherIndividual, individual, "CONNECTED");

        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(individual, otherIndividual.getId())).isPresent();
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(otherIndividual, individual.getId())).isPresent();

        //when
        mvc.perform(delete("/api/v1/connection/delete/" + individual.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(otherIndividualMockPrincipal))
                .andExpect(status().isOk());

        // then
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(individual, otherIndividual.getId())).isNotPresent();
        assertThat(connectionRepository.findConnectionByUserAndConnectedToId(otherIndividual, individual.getId())).isNotPresent();
    }

    private RegistrationDto getRegistrationDto(String email) {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail(email);
        registrationDto.setName("name");
        registrationDto.setPassword("password");
        registrationDto.setAddress("");

        return registrationDto;
    }

    private Connection getConnection(User user, User connectedToUser, String connectionStatus) {
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setConnectedToId(connectedToUser.getId());
        connection.setConnectionStatus(connectionStatus);
        connectionRepository.save(connection);
        return connection;
    }
}