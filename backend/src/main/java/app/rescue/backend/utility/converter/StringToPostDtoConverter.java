package app.rescue.backend.utility.converter;

import app.rescue.backend.payload.PostDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPostDtoConverter implements Converter<String, PostDto> {

    private final ObjectMapper objectMapper;

    public StringToPostDtoConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public PostDto convert(@NotNull String source) {
        return objectMapper.readValue(source, PostDto.class);
    }
}