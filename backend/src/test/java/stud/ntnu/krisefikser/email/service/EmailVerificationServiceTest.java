package stud.ntnu.krisefikser.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import stud.ntnu.krisefikser.config.FrontendConfig;
import stud.ntnu.krisefikser.email.config.MailProperties;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

  private static final String TEST_FRONTEND_URL = "http://localhost:3000";
  private static final long TOKEN_VALIDITY_HOURS = 24;
  @Mock
  private VerificationTokenRepository tokenRepository;
  @Mock
  private EmailService emailService;
  @Mock
  private MailProperties mailProperties;
  @Mock
  private FrontendConfig frontendConfig;
  @InjectMocks
  private EmailVerificationService emailVerificationService;
  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setEmail("test@example.com");
    testUser.setFirstName("Test");
    testUser.setEmailVerified(false);

    lenient().when(mailProperties.getVerificationTokenValidityHours())
        .thenReturn(TOKEN_VALIDITY_HOURS);
    lenient().when(frontendConfig.getUrl()).thenReturn(TEST_FRONTEND_URL);
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
    verify(mailProperties).getVerificationTokenValidityHours();
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
    verify(mailProperties).getVerificationTokenValidityHours();
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
  void verifyToken_AlreadyUsedToken() {
    // Arrange
    String tokenString = UUID.randomUUID().toString();
    VerificationToken token = VerificationToken.builder()
        .token(tokenString)
        .user(testUser)
        .expiryDate(LocalDateTime.now().plusHours(24))
        .used(true)
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
    ResponseEntity<String> response = emailVerificationService.sendVerificationEmail(testUser,
        token);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Email sent successfully", response.getBody());
    verify(emailService).sendEmail(
        eq(testUser.getEmail()),
        eq("Please verify your email address"),
        anyString()
    );
    verify(frontendConfig).getUrl();
    verify(mailProperties).getVerificationTokenValidityHours();
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

  @Test
  void sendAdminLoginVerificationEmail_Success() {
    // Arrange
    String verificationLink = TEST_FRONTEND_URL + "/admin-verifisering?token=test-token";
    ResponseEntity<String> expectedResponse = ResponseEntity.ok("Email sent successfully");

    when(emailService.sendEmail(anyString(), anyString(), anyString()))
        .thenReturn(expectedResponse);

    // Act
    ResponseEntity<String> response = emailVerificationService.sendAdminLoginVerificationEmail(
        testUser,
        verificationLink
    );

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Email sent successfully", response.getBody());
    verify(emailService).sendEmail(
        eq(testUser.getEmail()),
        eq("Admin Login Verification"),
        anyString()
    );
    verify(mailProperties).getVerificationTokenValidityHours();
  }

  @Test
  void sendAdminLoginVerificationEmail_ClientError() {
    // Arrange
    String verificationLink = TEST_FRONTEND_URL + "/admin-verifisering?token=test-token";
    String errorMessage = "Invalid email address";

    when(emailService.sendEmail(anyString(), anyString(), anyString()))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage));

    // Act
    ResponseEntity<String> response = emailVerificationService.sendAdminLoginVerificationEmail(
        testUser,
        verificationLink
    );

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(errorMessage, response.getBody());
    verify(mailProperties).getVerificationTokenValidityHours();
  }
}