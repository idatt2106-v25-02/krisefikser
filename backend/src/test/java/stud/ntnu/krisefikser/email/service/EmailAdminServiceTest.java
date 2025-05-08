package stud.ntnu.krisefikser.email.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
    void sendAdminInvitation_Success() throws IOException {
        // Arrange
        Map<String, String> expectedVariables = new HashMap<>();
        expectedVariables.put("link", testInviteLink);
        
        when(emailTemplateService.loadAndReplace(eq("admin-invite.html"), eq(expectedVariables)))
            .thenReturn(testTemplateContent);
        when(emailService.sendEmail(eq(testEmail), eq("Admin Invitation - Krisefikser"), eq(testTemplateContent)))
            .thenReturn(ResponseEntity.ok("Email sent successfully"));

        // Act
        ResponseEntity<String> response = emailAdminService.sendAdminInvitation(testEmail, testInviteLink);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email sent successfully", response.getBody());
        verify(emailTemplateService).loadAndReplace(eq("admin-invite.html"), eq(expectedVariables));
        verify(emailService).sendEmail(eq(testEmail), eq("Admin Invitation - Krisefikser"), eq(testTemplateContent));
    }

    @Test
    void sendAdminInvitation_TemplateServiceError() throws IOException {
        // Arrange
        when(emailTemplateService.loadAndReplace(anyString(), any()))
            .thenThrow(new IOException("Template not found"));

        // Act
        ResponseEntity<String> response = emailAdminService.sendAdminInvitation(testEmail, testInviteLink);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to send admin invitation"));
        assertTrue(response.getBody().contains("Template not found"));
    }

    @Test
    void sendAdminInvitation_EmailServiceError() throws IOException {
        // Arrange
        Map<String, String> expectedVariables = new HashMap<>();
        expectedVariables.put("link", testInviteLink);
        
        when(emailTemplateService.loadAndReplace(eq("admin-invite.html"), eq(expectedVariables)))
            .thenReturn(testTemplateContent);
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.internalServerError().body("Failed to send email"));

        // Act
        ResponseEntity<String> response = emailAdminService.sendAdminInvitation(testEmail, testInviteLink);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to send email", response.getBody());
    }

    @Test
    void sendAdminInvitation_NullEmail() {
        // Act
        ResponseEntity<String> response = emailAdminService.sendAdminInvitation(null, testInviteLink);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email and invite link cannot be null", response.getBody());
    }

    @Test
    void sendAdminInvitation_NullInviteLink() {
        // Act
        ResponseEntity<String> response = emailAdminService.sendAdminInvitation(testEmail, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email and invite link cannot be null", response.getBody());
    }
} 