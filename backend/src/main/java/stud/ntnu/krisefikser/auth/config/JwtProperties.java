package stud.ntnu.krisefikser.auth.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT (JSON Web Token) settings.
 *
 * <p>This class is used to bind the properties defined in the application configuration file
 * (e.g., application.yml or application.properties) to Java objects. It allows for easy access to
 * JWT related settings such as secret key and expiration times.
 *
 * @since 1.0
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@AllArgsConstructor
public class JwtProperties {

  private final String secret;
  private final Long accessTokenExpiration;
  private final Long refreshTokenExpiration;
  private final Long resetPasswordTokenExpiration;
}
