package stud.ntnu.krisefikser.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for customizing the ObjectMapper used in the application.
 *
 * <p>This class provides a custom ObjectMapper bean that registers the JavaTimeModule to handle
 * Java 8 date and time types.</p>
 */
@Configuration
public class JacksonConfig {

  /**
   * Creates a custom ObjectMapper bean.
   *
   * <p>This method creates and returns a new instance of ObjectMapper. The ObjectMapper is used
   * for serializing and deserializing JSON data. The JavaTimeModule is registered to support Java 8
   * date and time types.</p>
   *
   * @return a new instance of ObjectMapper with JavaTimeModule registered
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
}
