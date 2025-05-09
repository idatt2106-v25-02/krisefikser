package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.auth.config.JwtProperties;

class TokenServiceTest {

  private final static long ACCESS_TOKEN_EXPIRATION = 900_000; // 15 minutes
  private final static long REFRESH_TOKEN_EXPIRATION = 604_800_000; // 7 days
  private final static long RESET_PASSWORD_EXPIRATION = 3_600_000; // 1 hour
  private TokenService tokenService;
  private UserDetails userDetails;
  private JwtProperties jwtProperties;

  @BeforeEach
  void setUp() {
    jwtProperties = mock(JwtProperties.class);
    when(jwtProperties.getSecret()).thenReturn(
        "test-jwt-secret-key-that-is-at-least-32-characters-long-for-security");
    when(jwtProperties.getAccessTokenExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);
    when(jwtProperties.getResetPasswordTokenExpiration()).thenReturn(RESET_PASSWORD_EXPIRATION);

    tokenService = new TokenService(jwtProperties);

    userDetails = new User(
        "test@example.com",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }

  @Test
  void generateAccessToken_ShouldCreateValidAccessToken() {
    // Act
    String token = tokenService.generateAccessToken(userDetails);

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(tokenService.extractEmail(token)).isEqualTo("test@example.com");
    assertThat(tokenService.isExpired(token)).isFalse();
    assertThat(tokenService.isAccessToken(token)).isTrue();
    assertThat(tokenService.isRefreshToken(token)).isFalse();
    assertThat(tokenService.extractTokenType(token)).isEqualTo(TokenService.ACCESS_TOKEN);
  }

  @Test
  void generateRefreshToken_ShouldCreateValidRefreshToken() {
    // Act
    String token = tokenService.generateRefreshToken(userDetails);

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(tokenService.extractEmail(token)).isEqualTo("test@example.com");
    assertThat(tokenService.isExpired(token)).isFalse();
    assertThat(tokenService.isRefreshToken(token)).isTrue();
    assertThat(tokenService.isAccessToken(token)).isFalse();
    assertThat(tokenService.extractTokenType(token)).isEqualTo(TokenService.REFRESH_TOKEN);
  }

  @Test
  void generateResetPasswordToken_ShouldCreateValidToken() {
    // Act
    String token = tokenService.generateResetPasswordToken("reset@example.com");

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(tokenService.extractEmail(token)).isEqualTo("reset@example.com");
    assertThat(tokenService.isExpired(token)).isFalse();
    // Reset password token doesn't have a token type claim
    assertThat(tokenService.extractTokenType(token)).isNull();
  }

  @Test
  void generate_ShouldCreateValidToken() {
    // Arrange
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);

    // Act
    String token = tokenService.generate(userDetails, expirationDate, new HashMap<>());

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(tokenService.extractEmail(token)).isEqualTo("test@example.com");
    assertThat(tokenService.isExpired(token)).isFalse();
  }

  @Test
  void generate_WithClaims_ShouldIncludeClaimsInToken() {
    // Arrange
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);
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
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);
    String token = tokenService.generate(userDetails, expirationDate, new HashMap<>());

    // Act
    boolean valid = tokenService.isValid(token, userDetails);

    // Assert
    assertThat(valid).isTrue();
  }

  @Test
  void isValid_WithExpiredToken_ShouldReturnFalse() {
    // Arrange
    Date pastDate = new Date(System.currentTimeMillis() - 10000);
    String token = tokenService.generate(userDetails, pastDate, new HashMap<>());

    // Act
    boolean valid = tokenService.isValid(token, userDetails);

    // Assert
    assertThat(valid).isFalse();
  }

  @Test
  void isValid_WithDifferentUser_ShouldReturnFalse() {
    // Arrange
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);
    String token = tokenService.generate(userDetails, expirationDate, new HashMap<>());

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
  void isValid_WithNullSubject_ShouldReturnFalse() {
    // Mock a token with null subject
    String token = "invalid.token";

    // Act
    boolean valid = tokenService.isValid(token, userDetails);

    // Assert
    assertThat(valid).isFalse();
  }

  @Test
  void isAccessToken_WithInvalidToken_ShouldReturnFalse() {
    // Act
    boolean isAccessToken = tokenService.isAccessToken("invalid.token");

    // Assert
    assertThat(isAccessToken).isFalse();
  }

  @Test
  void isRefreshToken_WithInvalidToken_ShouldReturnFalse() {
    // Act
    boolean isRefreshToken = tokenService.isRefreshToken("invalid.token");

    // Assert
    assertThat(isRefreshToken).isFalse();
  }

  @Test
  void extractTokenType_WithValidToken_ShouldReturnTokenType() {
    // Arrange
    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    // Act & Assert
    assertThat(tokenService.extractTokenType(accessToken)).isEqualTo(TokenService.ACCESS_TOKEN);
    assertThat(tokenService.extractTokenType(refreshToken)).isEqualTo(TokenService.REFRESH_TOKEN);
  }

  @Test
  void extractTokenType_WithInvalidToken_ShouldReturnNull() {
    // Act
    String tokenType = tokenService.extractTokenType("invalid.token");

    // Assert
    assertThat(tokenType).isNull();
  }

  @Test
  void extractEmail_FromValidToken_ShouldReturnEmail() {
    // Arrange
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);
    String token = tokenService.generate(userDetails, expirationDate, new HashMap<>());

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
    Date expirationDate = new Date(System.currentTimeMillis() + 60000);
    String token = tokenService.generate(userDetails, expirationDate, new HashMap<>());

    // Act
    boolean expired = tokenService.isExpired(token);

    // Assert
    assertThat(expired).isFalse();
  }

  @Test
  void isExpired_WithPastExpirationDate_ShouldReturnTrue() {
    // Arrange
    Date pastDate = new Date(System.currentTimeMillis() - 10000);
    String token = tokenService.generate(userDetails, pastDate, new HashMap<>());

    // Act
    boolean expired = tokenService.isExpired(token);

    // Assert
    assertThat(expired).isTrue();
  }

  @Test
  void isExpired_WithInvalidToken_ShouldReturnTrue() {
    // Act
    boolean expired = tokenService.isExpired("invalid.token");

    // Assert
    assertThat(expired).isTrue();
  }
}