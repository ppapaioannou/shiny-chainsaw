package app.rescue.backend.utility;

import javax.mail.MessagingException;

public interface EmailSender {
    void send(String toEmail, String recipientName, String link) throws MessagingException;
}
