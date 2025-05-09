package stud.ntnu.krisefikser.email.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.email.service.MockEmailService;

/**
 * Configuration class for the email service.
 *
 * <p>This class defines the configuration for the email service, including the properties </p>
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class EmailServiceConfig {

  /**
   * Creates a bean of the EmailService implementation.
   *
   * <p>This method creates a bean of the EmailService implementation based on the active profile.
   * If the profile is "test", it returns a mock email service. Otherwise, it returns a real email
   * service.</p>
   *
   * @return an instance of EmailService
   */
  @Bean
  @Profile("test")
  public EmailService mockEmailService() {
    return new MockEmailService();
  }
}