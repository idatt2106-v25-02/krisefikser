package stud.ntnu.krisefikser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating a RestTemplate bean.
 *
 * <p>This class is responsible for configuring the RestTemplate used for making HTTP requests.
 * It provides a single bean of type RestTemplate that can be injected into other components.
 *
 * @since 1.0
 */
@Configuration
public class RestTemplateConfig {

  /**
   * Creates a RestTemplate bean.
   *
   * <p>This method creates and returns a new instance of RestTemplate. The RestTemplate is used
   * for making HTTP requests to external services.
   *
   * @return a new instance of RestTemplate
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
} 