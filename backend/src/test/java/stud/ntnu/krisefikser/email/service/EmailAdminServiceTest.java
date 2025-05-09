package stud.ntnu.krisefikser.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import stud.ntnu.krisefikser.email.exception.EmailSendingException;

@ExtendWith(MockitoExtension.class)
class EmailAdminServiceTest {

  @Mock
  private EmailService emailService;

  @Mock
  private EmailTemplateService emailTemplateService;

  @InjectMocks
  private EmailAdminService emailAdminService;

  private String testEmail;
  private String testInviteLink;
  private String testTemplateContent;

  @BeforeEach
  void setUp() {
    testEmail = "admin@example.com";
    testInviteLink = "http://localhost:5173/admin/registrer?token=test-token";
    testTemplateContent = "<html>Welcome to Krisefikser! Click here: {{link}}</html>";
  }

  @Test
  void sendAdminInvitation_Success() throws Exception {
    // Arrange
    Map<String, String> expectedVariables = new HashMap<>();
    expectedVariables.put("link", testInviteLink);

    when(emailTemplateService.loadAndReplace(eq("admin-invite.html"), eq(expectedVariables)))
        .thenReturn(testTemplateContent);
    when(emailService.sendEmail(eq(testEmail), eq("Admin Invitation - Krisefikser"),
        eq(testTemplateContent)))
        .thenReturn(ResponseEntity.ok("Email sent successfully"));

    // Act
    ResponseEntity<String> response = emailAdminService.sendAdminInvitation(testEmail,
        testInviteLink);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Email sent successfully", response.getBody());
    verify(emailTemplateService).loadAndReplace(eq("admin-invite.html"), eq(expectedVariables));
    verify(emailService).sendEmail(eq(testEmail), eq("Admin Invitation - Krisefikser"),
        eq(testTemplateContent));
  }

  @Test
  void sendAdminInvitation_NullEmail() {
    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailAdminService.sendAdminInvitation(null, testInviteLink)
    );

    assertEquals("Email or invite link is null", exception.getMessage());
    verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
  }

  @Test
  void sendAdminInvitation_NullInviteLink() {
    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailAdminService.sendAdminInvitation(testEmail, null)
    );

    assertEquals("Email or invite link is null", exception.getMessage());
    verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
  }

  @Test
  void sendAdminInvitation_EmailSendingExceptionPropagated() throws Exception {
    // Arrange
    Map<String, String> expectedVariables = new HashMap<>();
    expectedVariables.put("link", testInviteLink);

    when(emailTemplateService.loadAndReplace(eq("admin-invite.html"), eq(expectedVariables)))
        .thenReturn(testTemplateContent);

    EmailSendingException originalException = new EmailSendingException("Original error");
    when(emailService.sendEmail(anyString(), anyString(), anyString()))
        .thenThrow(originalException);

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailAdminService.sendAdminInvitation(testEmail, testInviteLink)
    );

    assertSame(originalException, exception);
  }

  @Test
  void sendAdminInvitation_GeneralException() throws Exception {
    // Arrange
    Map<String, String> expectedVariables = new HashMap<>();
    expectedVariables.put("link", testInviteLink);

    when(emailTemplateService.loadAndReplace(eq("admin-invite.html"), eq(expectedVariables)))
        .thenThrow(new RuntimeException("Some unexpected error"));

    // Act & Assert
    EmailSendingException exception = assertThrows(EmailSendingException.class, () ->
        emailAdminService.sendAdminInvitation(testEmail, testInviteLink)
    );

    assertEquals("Failed to send admin invitation email", exception.getMessage());
  }
}