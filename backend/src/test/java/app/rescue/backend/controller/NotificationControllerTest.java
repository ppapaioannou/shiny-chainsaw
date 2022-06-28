package app.rescue.backend.controller;

import app.rescue.backend.model.*;
import app.rescue.backend.payload.NotificationDto;
import app.rescue.backend.repository.NotificationRepository;
import app.rescue.backend.repository.UserRepository;
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
import java.util.Optional;

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
@AutoConfigureMockMvc(addFilters = false) // to ignore 403 errors
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;


    private User user;
    private User sender;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        user = getUser("user@example.com");
        sender = getUser("sender@example.com");
        mockPrincipal = mock(Principal.class);
        given(mockPrincipal.getName()).willReturn(user.getEmail());
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canGetAllNotifications() throws Exception {
        // given
        List<Notification> expected = new ArrayList<>();
        expected.add(createNotification(user, "1", sender));
        expected.add(createNotification(user, "2", sender));

        // when
        MvcResult getNotificationsResult = mvc.perform(get("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getNotificationsResult
                .getResponse()
                .getContentAsString();

        List<NotificationDto> actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(expected.size() -1 -i).getId(), actual.get(i).getId());
            assertEquals(sender.getName(), actual.get(i).getSender());
            assertEquals(expected.get(expected.size() -1 -i).getText(), actual.get(i).getText());
        }
    }

    @Test
    void getNumberOfUnreadNotifications() throws Exception {
        // given
        List<Notification> expected = new ArrayList<>();
        expected.add(createNotification(user, "1", sender));
        expected.add(createNotification(user, "2", sender));
        expected.add(createNotification(user, "3", sender));

        mvc.perform(put("/api/v1/notifications/" + expected.get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // when
        MvcResult getNotificationsResult = mvc.perform(get("/api/v1/notifications/unread")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String contentAsString = getNotificationsResult
                .getResponse()
                .getContentAsString();

        Integer actual = objectMapper.readValue(
                contentAsString,
                new TypeReference<>() {}
        );

        assertThat(actual).isEqualTo(expected.size()-1);
    }

    @Test
    void canReadNotification() throws Exception {
        // given
        Notification notification = createNotification(user, "1", sender);
        assertThat(notification.getReadAt()).isNull();

        // when
        mvc.perform(put("/api/v1/notifications/" + notification.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        Optional<Notification> readNotification = notificationRepository.findById(notification.getId());

        assertThat(readNotification).isPresent();
        assertThat(readNotification.get().getReadAt()).isNotNull();


    }

    @Test
    void canDeleteNotification() throws Exception {
        // given
        Notification notification = createNotification(user, "1", sender);

        assertThat(notificationRepository.findById(notification.getId())).isPresent();

        // when
        mvc.perform(delete("/api/v1/notifications/" + notification.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        // then
        Optional<Notification> readNotification = notificationRepository.findById(notification.getId());

        assertThat(readNotification).isNotPresent();
    }

    private User getUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setName("name");
        user.setUserRole(Role.INDIVIDUAL);
        userRepository.save(user);
        return user;
    }

    private Notification createNotification(User user, String text, User sender) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setText(text);
        notification.setSenderId(sender.getId());
        notification.setNotificationType("test");
        notificationRepository.save(notification);
        return notification;
    }
}