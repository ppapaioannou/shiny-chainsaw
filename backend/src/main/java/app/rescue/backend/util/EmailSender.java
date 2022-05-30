package app.rescue.backend.util;

public interface EmailSender {
    void send(String toEmail, String recipientName, String link);
}
