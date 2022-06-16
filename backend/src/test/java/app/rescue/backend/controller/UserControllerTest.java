package app.rescue.backend.controller;

import app.rescue.backend.model.Role;
import app.rescue.backend.model.User;
import app.rescue.backend.payload.LocationDto;
import app.rescue.backend.payload.RegistrationDto;
import app.rescue.backend.payload.UserDto;
import app.rescue.backend.repository.UserRepository;
import app.rescue.backend.service.UserService;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() throws Exception {
        RegistrationDto request = getRegistrationDto("user@example.com");
        String userRole = Role.INDIVIDUAL.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        user = userService.getUserByEmail(request.getEmail());

        mockPrincipal = mock(Principal.class);
        given(mockPrincipal.getName()).willReturn(user.getEmail());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void canInviteFriend() throws Exception {
        // given
        String email = "other-user@example.com";

        // when
        // then
        mvc.perform(post("/api/v1/users/ref/" + email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());
    }

    @Test
    void canGetAllUsers() throws Exception {
        // given
        List<User> expected = new ArrayList<>();
        expected.add(user);

        RegistrationDto request = getRegistrationDto("other-user@example.com");
        String userRole = Role.INDIVIDUAL.toString();

        mvc.perform(post("/api/v1/auth/register/" + userRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        expected.add(userService.getUserByEmail(request.getEmail()));

        // when
        MvcResult getAllUsersResult = mvc.perform(get("/api/v1/users/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getAllUsersResult
                .getResponse()
                .getContentAsString();

        List<UserDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(expected.size() -1 -i).getId(), actual.get(i).getId());
            assertEquals(expected.get(expected.size() -1 -i).getName(), actual.get(i).getName());
            assertEquals(expected.get(expected.size() -1 -i).getEmail(), actual.get(i).getEmail());
        }
    }

    @Test
    void canGetSingleUser() throws Exception {
        // given
        // when
        MvcResult getSingleUserResult = mvc.perform(get("/api/v1/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getSingleUserResult
                .getResponse()
                .getContentAsString();

        UserDto actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(user.getId(), actual.getId());
        assertEquals(user.getName(), actual.getName());
        assertEquals(user.getEmail(), actual.getEmail());
    }

    @Test
    void canUpdateUserLocation() throws Exception {
        // given
        LocationDto request = getLocationDto();

        // when
        mvc.perform(put("/api/v1/users/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        User updatedUser = userService.getUserByEmail(user.getEmail());

        assertEquals(Double.parseDouble(request.getLatitude()), updatedUser.getLocation().getCoordinate().x, 0.01);
        assertEquals(Double.parseDouble(request.getLongitude()), updatedUser.getLocation().getCoordinate().y, 0.01);
    }

    @Test
    void canUpdateUserInfo() throws Exception {
        // given
        UserDto request = getUserDto(user.getEmail());

        // when
        mvc.perform(put("/api/v1/users/update-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("payload", objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        User updatedUser = userService.getUserByEmail(user.getEmail());

        assertEquals(request.getName(), updatedUser.getName());
    }

    private RegistrationDto getRegistrationDto(String email) {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail(email);
        registrationDto.setName("name");
        registrationDto.setPassword("password");

        return registrationDto;
    }

    private LocationDto getLocationDto() {
        LocationDto locationDto = new LocationDto();
        locationDto.setLatitude("12");
        locationDto.setLongitude("-12");
        locationDto.setDiameterInMeters("1000");

        return locationDto;
    }

    private UserDto getUserDto(String email) {
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setName("new-name");

        return userDto;
    }
}