package app.rescue.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private EmailService underTest;

    private MimeMessage mimeMessage;


    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage((Session)null);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);
        underTest = new EmailService(mailSender);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canSendConfirmAccountEmail() throws MessagingException {
        String recipient = "example@example.com";
        String name = "name";
        String confirmationLink = "confirm-link";

        underTest.send(recipient, name, confirmationLink);

        assertEquals(recipient, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertThat(mimeMessage.getSubject()).isEqualTo("Confirm your email");

    }

    @Test
    void canSendInvitationEmail() throws MessagingException {
        String recipient = "example@example.com";
        String name = "name";
        String confirmationLink = "ref-link";


        underTest.send(recipient, name, confirmationLink);

        assertEquals(recipient, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertThat(mimeMessage.getSubject()).isEqualTo("Come join Rescue!");
    }

}