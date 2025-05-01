package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import stud.ntnu.krisefikser.auth.dto.TurnstileResponse;

import java.util.HashMap;
import java.util.Map;

public class TurnstileServiceTest {

    private TurnstileService turnstileService;
    private RestTemplate restTemplate;
    private final String testSecretKey = "test-secret-key";
    private final String testToken = "test-turnstile-token";
    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    @BeforeEach
    void setUp() {
        // Mock RestTemplate
        restTemplate = mock(RestTemplate.class);
        
        // Create TurnstileService with mocked dependencies
        turnstileService = new TurnstileService() {
            @Override
            public boolean verify(String token) {
                Map<String, String> body = new HashMap<>();
                body.put("secret", testSecretKey);
                body.put("response", token);

                ResponseEntity<TurnstileResponse> response = restTemplate.postForEntity(
                    VERIFY_URL,
                    body,
                    TurnstileResponse.class
                );

                return response.getBody() != null && response.getBody().isSuccess();
            }
        };
    }

    @Test
    void verify_WithValidToken_ShouldReturnTrue() {
        // Arrange
        TurnstileResponse successResponse = new TurnstileResponse();
        successResponse.setSuccess(true);
        
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(
            successResponse,
            HttpStatus.OK
        );
        
        when(restTemplate.postForEntity(
            eq(VERIFY_URL),
            any(),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(testToken);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void verify_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        TurnstileResponse failureResponse = new TurnstileResponse();
        failureResponse.setSuccess(false);
        
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(
            failureResponse,
            HttpStatus.OK
        );
        
        when(restTemplate.postForEntity(
            eq(VERIFY_URL),
            any(),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(testToken);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_WithNullResponse_ShouldReturnFalse() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(
            null,
            HttpStatus.OK
        );
        
        when(restTemplate.postForEntity(
            eq(VERIFY_URL),
            any(),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(testToken);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_WithErrorResponse_ShouldReturnFalse() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(
            null,
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        
        when(restTemplate.postForEntity(
            eq(VERIFY_URL),
            any(),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(testToken);

        // Assert
        assertThat(result).isFalse();
    }
}
