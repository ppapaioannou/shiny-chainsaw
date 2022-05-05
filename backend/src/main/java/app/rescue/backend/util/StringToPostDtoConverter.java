package app.rescue.backend.util;

import app.rescue.backend.payload.PostDto;
import app.rescue.backend.payload.request.PostRequest;
import app.rescue.backend.payload.request.TestRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

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