package app.rescue.backend.utility;

public interface EmailSender {
    void send(String toEmail, String recipientName, String link);
}
