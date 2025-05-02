package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import stud.ntnu.krisefikser.auth.dto.TurnstileResponse;

@ExtendWith(MockitoExtension.class)
public class TurnstileServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TurnstileService turnstileService;

    private String validToken;
    private String invalidToken;
    private TurnstileResponse successResponse;
    private TurnstileResponse failureResponse;
    private Map<String, String> expectedBody;

    @BeforeEach
    void setUp() {
        validToken = "valid-turnstile-token";
        invalidToken = "invalid-turnstile-token";

        successResponse = new TurnstileResponse();
        successResponse.setSuccess(true);

        failureResponse = new TurnstileResponse();
        failureResponse.setSuccess(false);

        expectedBody = new HashMap<>();
        expectedBody.put("secret", "test-secret-key");
        expectedBody.put("response", validToken);
    }

    @Test
    void verify_WithValidToken_ShouldReturnTrue() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(
            eq(TurnstileService.VERIFY_URL),
            any(Map.class),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(validToken);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void verify_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(failureResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(
            eq(TurnstileService.VERIFY_URL),
            any(Map.class),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(invalidToken);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_WithNullResponse_ShouldReturnFalse() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity(
            eq(TurnstileService.VERIFY_URL),
            any(Map.class),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(validToken);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_WithServerError_ShouldReturnFalse() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(
            eq(TurnstileService.VERIFY_URL),
            any(Map.class),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(validToken);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_WithBadRequest_ShouldReturnFalse() {
        // Arrange
        ResponseEntity<TurnstileResponse> responseEntity = new ResponseEntity<>(failureResponse, HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(
            eq(TurnstileService.VERIFY_URL),
            any(Map.class),
            eq(TurnstileResponse.class)
        )).thenReturn(responseEntity);

        // Act
        boolean result = turnstileService.verify(validToken);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void verify_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean result = turnstileService.verify("");

        // Assert
        assertThat(result).isFalse();
    }
}
