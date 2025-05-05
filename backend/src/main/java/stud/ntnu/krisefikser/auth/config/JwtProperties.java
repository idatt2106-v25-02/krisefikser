package stud.ntnu.krisefikser.auth.config;

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
public class JwtProperties {

  private final String secret;
  private final Long accessTokenExpiration;
  private final Long refreshTokenExpiration;

  /**
   * Constructs a new JwtProperties object with the specified secret key and expiration times.
   *
   * @param secret                 the secret key used for signing JWTs
   * @param accessTokenExpiration  the expiration time for access tokens in milliseconds
   * @param refreshTokenExpiration the expiration time for refresh tokens in milliseconds
   */
  public JwtProperties(String secret, Long accessTokenExpiration, Long refreshTokenExpiration) {
    this.secret = secret;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }
}
