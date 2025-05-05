package stud.ntnu.krisefikser.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.lang.Collections;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
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

  private final SecretKey secretKey;

  public TokenService(JwtProperties jwtProperties) {
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
  }

  public String generate(UserDetails userDetails, Date expirationDate) {
    return generate(userDetails, expirationDate, Collections.emptyMap());
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

  public boolean isValid(String token, UserDetails userDetails) {
    final String username = extractEmail(token);
    return username != null && username.equals(userDetails.getUsername()) && !isExpired(token);
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
