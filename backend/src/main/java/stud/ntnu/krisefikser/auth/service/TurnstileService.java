package stud.ntnu.krisefikser.auth.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stud.ntnu.krisefikser.auth.dto.TurnstileResponse;

/**
 * Service class for handling Cloudflare Turnstile verification. This service is responsible for
 * verifying user interactions with Cloudflare Turnstile, which helps protect against automated
 * abuse and spam.
 *
 * <p>The service communicates with Cloudflare's Turnstile API to verify that a user interaction is
 * legitimate and not automated. It requires a secret key for authentication with the API.
 * </p>
 *
 * @see <a href="https://developers.cloudflare.com/turnstile/">Turnstile Documentation</a>
 */
@Service
public class TurnstileService {

  /**
   * The URL endpoint for Cloudflare Turnstile verification. This is the API endpoint where
   * verification requests are sent.
   */
  public static final String VERIFY_URL = "https://challenges.cloudflare"
      + ".com/turnstile/v0/siteverify";

  /**
   * RestTemplate instance for making HTTP requests to the Turnstile API. Injected through
   * constructor to allow for better testability.
   */
  @Autowired
  private final RestTemplate restTemplate;

  /**
   * The secret key for Turnstile verification, injected from application properties. This key is
   * used to authenticate requests to the Turnstile API. In test environment, it defaults to
   * "test-secret-key".
   */
  @Value("${turnstile.secret:test-secret-key}")
  private String secretKey;

  /**
   * Constructs a new TurnstileService with the specified RestTemplate.
   *
   * @param restTemplate the RestTemplate to use for making HTTP requests
   */
  public TurnstileService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Verifies a Turnstile token with Cloudflare's verification service.
   *
   * <p>This method sends a verification request to Cloudflare's Turnstile API with the provided
   * token and the configured secret key. It handles various error cases and returns false if
   * verification fails for any reason. It returns true if the token is valid and verification is
   * successful, false if the token is invalid, empty, or if verification fails for any other reason
   * (e.g., network error, API error)
   * </p>
   *
   * @param token The Turnstile token to verify, obtained from the client-side Turnstile widget.
   *              Must not be null or empty.
   * @return true if the token is valid and verification is successful, false otherwise.
   * @throws IllegalArgumentException if the token is null
   */
  public boolean verify(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    Map<String, String> body = new HashMap<>();
    body.put("secret", secretKey);
    body.put("response", token);

    try {
      ResponseEntity<TurnstileResponse> response = restTemplate.postForEntity(
          VERIFY_URL,
          body,
          TurnstileResponse.class
      );

      return response.getBody() != null && response.getBody().isSuccess();
    } catch (Exception e) {
      return false;
    }
  }
}
