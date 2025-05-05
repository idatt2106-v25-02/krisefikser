package stud.ntnu.krisefikser.email.app_config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for setting up email-related beans in the application context. This class
 * defines the necessary configuration for email communication.
 */
@Configuration
public class EmailConfig {

  /**
   * Creates and provides a RestTemplate bean configured for email-related operations. This method
   * registers a RestTemplate instance in the application context to be used for making RESTful
   * calls related to email functionalities.
   *
   * @return a new instance of RestTemplate to be used for email-related REST operations
   */
  @Bean
  public RestTemplate emailRestTemplate() {
    return new RestTemplate();
  }
} 