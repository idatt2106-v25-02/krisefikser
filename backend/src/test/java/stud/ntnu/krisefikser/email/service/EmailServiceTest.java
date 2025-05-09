package stud.ntnu.krisefikser.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import stud.ntnu.krisefikser.email.config.MailProperties;
import stud.ntnu.krisefikser.email.exception.EmailSendingException;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  private static final String TEST_HOST = "test.mailtrap.io";
  private static final String TEST_API_KEY = "test-api-key";
  private static final String TEST_FROM_EMAIL = "test@example.com";
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private MailProperties mailProperties;
  @InjectMocks
  private EmailService emailService;
  @Captor
  private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

  @BeforeEach
  void setUp() {
    lenient().when(mailProperties.getHost()).thenReturn(TEST_HOST);
    lenient().when(mailProperties.getApiKey()).thenReturn(TEST_API_KEY);
    lenient().when(mailProperties.getFrom()).thenReturn(TEST_FROM_EMAIL);
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

    verify(restTemplate).postForEntity(
        eq("https://" + TEST_HOST + "/api/send"),
        httpEntityCaptor.capture(),
        eq(String.class)
    );

    // Verify request payload contains correct data
    HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
    assertTrue(capturedEntity.getHeaders().containsKey("Authorization"));
    assertNotNull(capturedEntity.getBody());
  }

  @Test
  void sendEmail_NonSuccessfulResponse() {
    // Arrange
    String toEmail = "recipient@example.com";
    String subject = "Test Subject";
    String htmlContent = "<p>Test content</p>";

    ResponseEntity<String> errorResponse = ResponseEntity.badRequest()
        .body("Invalid email address");
    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(errorResponse);

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailService.sendEmail(toEmail, subject, htmlContent)
    );

    assertEquals("Failed to send email due to an unexpected error", exception.getMessage());
  }

  @Test
  void sendEmail_RestTemplateException() {
    // Arrange
    String toEmail = "recipient@example.com";
    String subject = "Test Subject";
    String htmlContent = "<p>Test content</p>";

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new RestClientException("Connection failed"));

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailService.sendEmail(toEmail, subject, htmlContent)
    );

    assertEquals("Failed to send email due to an unexpected error", exception.getMessage());
  }

  @Test
  void sendEmail_NullEmail() {
    // Arrange
    String subject = "Test Subject";
    String htmlContent = "<p>Test content</p>";

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailService.sendEmail(null, subject, htmlContent)
    );

    assertEquals("Recipient email cannot be null", exception.getMessage());
    verify(restTemplate, never()).postForEntity(anyString(), any(), any());
  }

  @Test
  void sendEmail_NullSubject() {
    // Arrange
    String toEmail = "recipient@example.com";
    String htmlContent = "<p>Test content</p>";

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailService.sendEmail(toEmail, null, htmlContent)
    );

    assertEquals("Email subject cannot be null", exception.getMessage());
    verify(restTemplate, never()).postForEntity(anyString(), any(), any());
  }

  @Test
  void sendEmail_NullHtmlContent() {
    // Arrange
    String toEmail = "recipient@example.com";
    String subject = "Test Subject";

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailService.sendEmail(toEmail, subject, null)
    );

    assertEquals("Email content cannot be null", exception.getMessage());
    verify(restTemplate, never()).postForEntity(anyString(), any(), any());
  }
}