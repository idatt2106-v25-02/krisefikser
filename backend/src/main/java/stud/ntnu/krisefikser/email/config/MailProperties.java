package stud.ntnu.krisefikser.email.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mailtrap email service.
 *
 * <p>This class holds the properties required to connect to the Mailtrap API, including the
 * host, API key, and default sender email address. </p>
 */
@ConfigurationProperties(prefix = "mail")
@Getter
@AllArgsConstructor
public class MailProperties {

  private final String host;
  private final String apiKey;
  private final String from;
  private final Long verificationTokenValidityHours;
}
