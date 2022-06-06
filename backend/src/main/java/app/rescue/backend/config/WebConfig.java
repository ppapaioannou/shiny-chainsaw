package app.rescue.backend.config;

import app.rescue.backend.utility.converter.StringToPostDtoConverter;
import app.rescue.backend.utility.converter.StringToRegistrationDtoConverter;
import app.rescue.backend.utility.converter.StringToUserDtoConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("*")
                .maxAge(3600L)
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        ObjectMapper mapper = new ObjectMapper();
        registry.addConverter(new StringToPostDtoConverter(mapper));
        registry.addConverter(new StringToRegistrationDtoConverter(mapper));
        registry.addConverter(new StringToUserDtoConverter(mapper));
    }


}
