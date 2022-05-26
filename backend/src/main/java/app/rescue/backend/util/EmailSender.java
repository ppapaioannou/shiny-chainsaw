package app.rescue.backend.util;

public interface EmailSender {
    void send(String to, String email, Boolean ref);
}
