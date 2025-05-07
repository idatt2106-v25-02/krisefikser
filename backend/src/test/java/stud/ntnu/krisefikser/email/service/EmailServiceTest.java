package stud.ntnu.krisefikser.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_HOST = "test.mailtrap.io";
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_FROM_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "mailtrapHost", TEST_HOST);
        ReflectionTestUtils.setField(emailService, "mailtrapApiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(emailService, "defaultFromEmail", TEST_FROM_EMAIL);
    }

    @Test
    void sendEmail_Success() {
        // Arrange
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String htmlContent = "<p>Test content</p>";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Email sent successfully");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = emailService.sendEmail(toEmail, subject, htmlContent);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email sent successfully", response.getBody());
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sendEmail_ClientError() {
        // Arrange
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String htmlContent = "<p>Test content</p>";
        String errorMessage = "Invalid email address";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", errorMessage.getBytes(), null));

        // Act
        ResponseEntity<String> response = emailService.sendEmail(toEmail, subject, htmlContent);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void sendEmail_RestClientException() {
        // Arrange
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String htmlContent = "<p>Test content</p>";
        String errorMessage = "Connection failed";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException(errorMessage));

        // Act
        ResponseEntity<String> response = emailService.sendEmail(toEmail, subject, htmlContent);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to send email"));
        assertTrue(response.getBody().contains(errorMessage));
    }
} 