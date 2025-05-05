package stud.ntnu.krisefikser.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.lang.Collections;
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

@Service
@Slf4j
public class TokenService {

  public static final String TOKEN_TYPE_CLAIM = "token_type";
  public static final String ACCESS_TOKEN = "ACCESS";
  public static final String REFRESH_TOKEN = "REFRESH";
  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

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

  public String generateAccessToken(UserDetails userDetails) {
    Date expirationDate = getAccessTokenExpiration();
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN);
    return generate(userDetails, expirationDate, claims);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    Date expirationDate = getRefreshTokenExpiration();
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN);
    return generate(userDetails, expirationDate, claims);
  }

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

  public String generate(UserDetails userDetails, Date expirationDate) {
    return generate(userDetails, expirationDate, Collections.emptyMap());
  }

  public boolean isValid(String token, UserDetails userDetails) {
    final String username = extractEmail(token);
    return username != null && username.equals(userDetails.getUsername()) && !isExpired(token);
  }

  public boolean isAccessToken(String token) {
    try {
      String tokenType = extractTokenType(token);
      return ACCESS_TOKEN.equals(tokenType);
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isRefreshToken(String token) {
    try {
      String tokenType = extractTokenType(token);
      return REFRESH_TOKEN.equals(tokenType);
    } catch (Exception e) {
      return false;
    }
  }

  public String extractTokenType(String token) {
    try {
      return getAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    } catch (Exception e) {
      log.warn("Failed to extract token type from token: {}", e.getMessage());
      return null;
    }
  }

  public String extractEmail(String token) {
    try {
      return getAllClaims(token).getSubject();
    } catch (Exception e) {
      log.warn("Failed to extract email from token: {}", e.getMessage());
      return null;
    }
  }

  public boolean isExpired(String token) {
    try {
      return getAllClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    } catch (Exception e) {
      return true;
    }
  }

  private Claims getAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
