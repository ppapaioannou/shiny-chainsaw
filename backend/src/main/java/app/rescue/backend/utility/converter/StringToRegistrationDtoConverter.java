package app.rescue.backend.utility.converter;

import app.rescue.backend.payload.RegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRegistrationDtoConverter implements Converter<String, RegistrationDto> {

    private final ObjectMapper objectMapper;

    public StringToRegistrationDtoConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public RegistrationDto convert(@NotNull String source) {
        return objectMapper.readValue(source, RegistrationDto.class);
    }
}
