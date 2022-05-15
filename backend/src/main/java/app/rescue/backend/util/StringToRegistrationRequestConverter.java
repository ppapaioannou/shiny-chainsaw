package app.rescue.backend.util;

import app.rescue.backend.payload.request.RegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRegistrationRequestConverter implements Converter<String, RegistrationRequest> {

    private final ObjectMapper objectMapper;

    public StringToRegistrationRequestConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public RegistrationRequest convert(@NotNull String source) {
        return objectMapper.readValue(source, RegistrationRequest.class);
    }
}
