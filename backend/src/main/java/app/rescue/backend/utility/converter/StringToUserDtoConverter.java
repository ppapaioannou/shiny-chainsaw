package app.rescue.backend.utility.converter;

import app.rescue.backend.payload.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserDtoConverter implements Converter<String, UserDto> {

    private final ObjectMapper objectMapper;

    public StringToUserDtoConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public UserDto convert(@NotNull String source) {
        return objectMapper.readValue(source, UserDto.class);
    }
}
