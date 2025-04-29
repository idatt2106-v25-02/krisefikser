package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.auth.config.JwtProperties;

public class TokenServiceTest {

  private TokenService tokenService;
  private UserDetails userDetails;
  private Date expirationDate;

  @BeforeEach
  void setUp() {
    JwtProperties jwtProperties = mock(JwtProperties.class);
    when(jwtProperties.getSecret()).thenReturn(
        "test-jwt-secret-key-that-is-at-least-32-characters-long-for-security");

    tokenService = new TokenService(jwtProperties);

    userDetails = new User(
        "test@example.com",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    expirationDate = new Date(System.currentTimeMillis() + 60000); // 1 minute in the future
  }

  @Test
  void generate_ShouldCreateValidToken() {
    // Act
    String token = tokenService.generate(userDetails, expirationDate);

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(tokenService.extractEmail(token)).isEqualTo("test@example.com");
    assertThat(tokenService.isExpired(token)).isFalse();
  }

  @Test
  void generate_WithClaims_ShouldIncludeClaimsInToken() {
    // Arrange
    Map<String, Object> claims = Map.of("customClaim", "customValue");

    // Act
    String token = tokenService.generate(userDetails, expirationDate, claims);

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(tokenService.extractEmail(token)).isEqualTo("test@example.com");
  }

  @Test
  void isValid_WithValidToken_ShouldReturnTrue() {
    // Arrange
    String token = tokenService.generate(userDetails, expirationDate);

    // Act
    boolean valid = tokenService.isValid(token, userDetails);

    // Assert
    assertThat(valid).isTrue();
  }

  @Test
  void isValid_WithExpiredToken_ShouldReturnFalse() {
    // Arrange
    Date pastDate = new Date(System.currentTimeMillis() - 10000); // 10 seconds in the past
    String token = tokenService.generate(userDetails, pastDate);

    // Act
    boolean valid = tokenService.isValid(token, userDetails);

    // Assert
    assertThat(valid).isFalse();
  }

  @Test
  void isValid_WithDifferentUser_ShouldReturnFalse() {
    // Arrange
    String token = tokenService.generate(userDetails, expirationDate);

    UserDetails otherUser = new User(
        "other@example.com",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    // Act
    boolean valid = tokenService.isValid(token, otherUser);

    // Assert
    assertThat(valid).isFalse();
  }

  @Test
  void extractEmail_FromValidToken_ShouldReturnEmail() {
    // Arrange
    String token = tokenService.generate(userDetails, expirationDate);

    // Act
    String email = tokenService.extractEmail(token);

    // Assert
    assertThat(email).isEqualTo("test@example.com");
  }

  @Test
  void extractEmail_FromInvalidToken_ShouldReturnNull() {
    // Act
    String email = tokenService.extractEmail("invalid.token.format");

    // Assert
    assertThat(email).isNull();
  }

  @Test
  void isExpired_WithFutureExpirationDate_ShouldReturnFalse() {
    // Arrange
    String token = tokenService.generate(userDetails, expirationDate);

    // Act
    boolean expired = tokenService.isExpired(token);

    // Assert
    assertThat(expired).isFalse();
  }

  @Test
  void isExpired_WithPastExpirationDate_ShouldReturnTrue() {
    // Arrange
    Date pastDate = new Date(System.currentTimeMillis() - 10000); // 10 seconds in the past
    String token = tokenService.generate(userDetails, pastDate);

    // Act
    boolean expired = tokenService.isExpired(token);

    // Assert
    assertThat(expired).isTrue();
  }
}