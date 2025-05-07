package stud.ntnu.krisefikser.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.config.JwtProperties;

/**
 * Service class for generating and validating JWT tokens.
 *
 * <p>This class provides methods to create JWT tokens, validate them, and extract information from
 * them.
 */
@Service
@Slf4j
public class TokenService {

  public static final String TOKEN_TYPE_CLAIM = "token_type";
  public static final String ACCESS_TOKEN = "ACCESS";
  public static final String REFRESH_TOKEN = "REFRESH";
  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  /**
   * Constructs a new TokenService with the specified JwtProperties.
   *
   * @param jwtProperties the properties containing the JWT secret key
   */
  public TokenService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
  }

  private Date getAccessTokenExpiration() {
    return new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration());
  }

  private Date getRefreshTokenExpiration() {
    return new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration());
  }

  private Date getResetPasswordTokenExpiration() {
    return new Date(System.currentTimeMillis() + jwtProperties.getResetPasswordTokenExpiration());
  }

  /**
   * Generates an access token for the given user details.
   *
   * @param userDetails the user details to include in the token
   * @return the generated access token
   */
  public String generateAccessToken(UserDetails userDetails) {
    Date expirationDate = getAccessTokenExpiration();
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN);
    return generate(userDetails, expirationDate, claims);
  }

  /**
   * Generates a refresh token for the given user details.
   *
   * @param userDetails the user details to include in the token
   * @return the generated refresh token
   */
  public String generateRefreshToken(UserDetails userDetails) {
    Date expirationDate = getRefreshTokenExpiration();
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN);
    return generate(userDetails, expirationDate, claims);
  }

  /**
   * Generates a reset password token for the given email.
   *
   * @param email the email to include in the token
   * @return the generated reset password token
   */
  public String generateResetPasswordToken(String email) {
    Date now = new Date(System.currentTimeMillis());
    Date expiration = getResetPasswordTokenExpiration();
    return Jwts.builder()
        .subject(email)
        .issuedAt(now)
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Generates a JWT token for the given user details and expiration date.
   *
   * @param userDetails      the user details to include in the token
   * @param expirationDate   the expiration date of the token
   * @param additionalClaims additional claims to include in the token
   * @return the generated JWT token
   */
  public String generate(UserDetails userDetails, Date expirationDate,
      Map<String, Object> additionalClaims) {
    Set<String> roles = userDetails.getAuthorities().stream()
        .map(authority -> authority.getAuthority().replace("ROLE_", "")).collect(
            Collectors.toSet());

    return Jwts.builder().subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis())).expiration(expirationDate)
        .claim("roles", roles).claims(additionalClaims).signWith(secretKey)
        .compact();
  }

  /**
   * Validates the token against the user details.
   *
   * @param token       the JWT token
   * @param userDetails the user details to validate against
   * @return true if the token is valid, false otherwise
   */
  public boolean isValid(String token, UserDetails userDetails) {
    final String username = extractEmail(token);
    return username != null && username.equals(userDetails.getUsername()) && !isExpired(token);
  }

  /**
   * Validates a given access token.
   *
   * @param token the JWT token
   * @return true if the token is a valid access token, false otherwise
   */
  public boolean isAccessToken(String token) {
    try {
      String tokenType = extractTokenType(token);
      return ACCESS_TOKEN.equals(tokenType);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Validates a given refresh token.
   *
   * @param token the JWT token
   * @return true if the token is a valid refresh token, false otherwise
   */
  public boolean isRefreshToken(String token) {
    try {
      String tokenType = extractTokenType(token);
      return REFRESH_TOKEN.equals(tokenType);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extracts the token type from the token.
   *
   * @param token the JWT token
   * @return the token type extracted from the token
   */
  public String extractTokenType(String token) {
    try {
      return getAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    } catch (Exception e) {
      log.warn("Failed to extract token type from token: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extracts the email from the token.
   *
   * @param token the JWT token
   * @return the email extracted from the token
   */
  public String extractEmail(String token) {
    try {
      return getAllClaims(token).getSubject();
    } catch (Exception e) {
      log.warn("Failed to extract email from token: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Checks if the token is expired.
   *
   * @param token the JWT token
   * @return true if the token is expired, false otherwise
   */
  public boolean isExpired(String token) {
    try {
      return getAllClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Extracts all claims from the token.
   *
   * @param token the JWT token
   * @return the claims extracted from the token
   */
  private Claims getAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
