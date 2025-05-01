package stud.ntnu.krisefikser.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stud.ntnu.krisefikser.auth.dto.TurnstileResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for handling Cloudflare Turnstile verification.
 * This service is responsible for verifying user interactions with Cloudflare Turnstile,
 * which helps protect against automated abuse and spam.
 */
@Service
public class TurnstileService {

    /** The URL endpoint for Cloudflare Turnstile verification */
    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    /** RestTemplate instance for making HTTP requests */
    private final RestTemplate restTemplate = new RestTemplate();

    /** The secret key for Turnstile verification, injected from application properties */
    @Value("${turnstile.secret}")
    private String secretKey;

    /**
     * Verifies a Turnstile token with Cloudflare's verification service.
     *
     * @param token The Turnstile token to verify
     * @return true if the token is valid and verification is successful, false otherwise
     */
    public boolean verify(String token) {
        Map<String, String> body = new HashMap<>();
        body.put("secret", secretKey);
        body.put("response", token);

        ResponseEntity<TurnstileResponse> response = restTemplate.postForEntity(
            VERIFY_URL,
            body,
            TurnstileResponse.class
        );

        return response.getBody() != null && response.getBody().isSuccess();
    }
}
