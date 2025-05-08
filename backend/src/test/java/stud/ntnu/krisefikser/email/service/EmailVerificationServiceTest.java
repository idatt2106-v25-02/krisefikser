package stud.ntnu.krisefikser.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User testUser;
    private static final String TEST_FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setEmailVerified(false);

        ReflectionTestUtils.setField(emailVerificationService, "tokenValidityHours", 24);
        ReflectionTestUtils.setField(emailVerificationService, "frontendUrl", TEST_FRONTEND_URL);
    }

    @Test
    void createVerificationToken_Success() {
        // Arrange
        when(tokenRepository.findByUserAndUsed(any(User.class), eq(false)))
                .thenReturn(Optional.empty());
        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        VerificationToken token = emailVerificationService.createVerificationToken(testUser);

        // Assert
        assertNotNull(token);
        assertNotNull(token.getToken());
        assertFalse(token.isUsed());
        assertTrue(token.getExpiryDate().isAfter(LocalDateTime.now()));
        assertEquals(testUser, token.getUser());
        verify(tokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void createVerificationToken_DeletesExistingToken() {
        // Arrange
        VerificationToken existingToken = new VerificationToken();
        when(tokenRepository.findByUserAndUsed(any(User.class), eq(false)))
                .thenReturn(Optional.of(existingToken));
        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        VerificationToken newToken = emailVerificationService.createVerificationToken(testUser);

        // Assert
        verify(tokenRepository).delete(existingToken);
        assertNotNull(newToken);
        assertNotEquals(existingToken, newToken);
    }

    @Test
    void verifyToken_ValidToken() {
        // Arrange
        String tokenString = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenString)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        when(tokenRepository.findByToken(tokenString))
                .thenReturn(Optional.of(token));
        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        boolean result = emailVerificationService.verifyToken(tokenString);

        // Assert
        assertTrue(result);
        assertTrue(token.isUsed());
        assertTrue(testUser.isEmailVerified());
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyToken_InvalidToken() {
        // Arrange
        String tokenString = "invalid-token";
        when(tokenRepository.findByToken(tokenString))
                .thenReturn(Optional.empty());

        // Act
        boolean result = emailVerificationService.verifyToken(tokenString);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).save(any(VerificationToken.class));
    }

    @Test
    void verifyToken_ExpiredToken() {
        // Arrange
        String tokenString = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenString)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByToken(tokenString))
                .thenReturn(Optional.of(token));

        // Act
        boolean result = emailVerificationService.verifyToken(tokenString);

        // Assert
        assertFalse(result);
        verify(tokenRepository, never()).save(any(VerificationToken.class));
    }

    @Test
    void sendVerificationEmail_Success() {
        // Arrange
        String tokenString = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenString)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Email sent successfully");
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = emailVerificationService.sendVerificationEmail(testUser, token);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email sent successfully", response.getBody());
        verify(emailService).sendEmail(
                eq(testUser.getEmail()),
                eq("Please verify your email address"),
                anyString()
        );
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Arrange
        String resetLink = TEST_FRONTEND_URL + "/verifiser-passord-tilbakestilling?token=test-token";
        long expirationHours = 24;
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Email sent successfully");

        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = emailVerificationService.sendPasswordResetEmail(
            testUser,
            resetLink,
            expirationHours
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email sent successfully", response.getBody());
        verify(emailService).sendEmail(
            eq(testUser.getEmail()),
            eq("Reset your password"),
            anyString()
        );
    }

    @Test
    void sendPasswordResetEmail_ClientError() {
        // Arrange
        String resetLink = TEST_FRONTEND_URL + "/verifiser-passord-tilbakestilling?token=test-token";
        long expirationHours = 24;
        String errorMessage = "Invalid email address";

        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage));

        // Act
        ResponseEntity<String> response = emailVerificationService.sendPasswordResetEmail(
            testUser,
            resetLink,
            expirationHours
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
} 