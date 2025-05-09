package stud.ntnu.krisefikser.auth.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Turnstile settings.
 *
 * <p>This class is used to bind the turnstile properties defined in the application configuration
 * file</p>
 */
@ConfigurationProperties(prefix = "turnstile")
@Getter
@AllArgsConstructor
public class TurnstileProperties {

  private final String turnstileSecretKey;
}
