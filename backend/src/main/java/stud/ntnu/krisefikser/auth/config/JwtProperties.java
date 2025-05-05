package stud.ntnu.krisefikser.auth.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
public class JwtProperties {

  private final String secret;
  private final Long accessTokenExpiration;
  private final Long refreshTokenExpiration;

  public JwtProperties(String secret, Long accessTokenExpiration, Long refreshTokenExpiration) {
    this.secret = secret;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }
}
