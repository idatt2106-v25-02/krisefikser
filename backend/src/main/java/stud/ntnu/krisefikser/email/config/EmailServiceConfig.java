package stud.ntnu.krisefikser.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.email.service.MockEmailService;

@Configuration
public class EmailServiceConfig {

  @Bean
  @Profile("test")
  public EmailService mockEmailService() {
    return new MockEmailService();
  }
}