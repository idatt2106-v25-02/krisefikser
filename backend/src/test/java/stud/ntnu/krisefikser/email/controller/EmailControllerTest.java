package stud.ntnu.krisefikser.email.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import stud.ntnu.krisefikser.email.dto.EmailRequestDto;
import stud.ntnu.krisefikser.email.service.EmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    private EmailRequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        validRequestDto = new EmailRequestDto(
            "test@example.com",
            "Test User",
            "http://localhost:3000/verify?token=test-token"
        );
    }

    @Test
    void sendEmail_Success() {
        // Arrange
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Email sent successfully");
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = emailController.sendEmail(validRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email sent successfully", response.getBody());
        verify(emailService).sendEmail(
                eq(validRequestDto.toEmail()),
                eq("Please verify your email"),
                eq(validRequestDto.verificationLink())
        );
    }

    @Test
    void sendEmail_ServiceThrowsException() {
        // Arrange
        String errorMessage = "Failed to send email";
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<String> response = emailController.sendEmail(validRequestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Email sending failed"));
        assertTrue(response.getBody().contains(errorMessage));
    }
} 