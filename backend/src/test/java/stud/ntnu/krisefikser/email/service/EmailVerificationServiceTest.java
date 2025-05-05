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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use Mockito with JUnit 5
class EmailVerificationServiceTest {

    @Mock // Create a mock instance of EmailService
    private EmailService emailServiceMock;

    @Mock // Create a mock instance of VerificationTokenRepository
    private VerificationTokenRepository tokenRepositoryMock;

    @InjectMocks // Create an instance of EmailVerificationService and inject the mocks into it
    private EmailVerificationService emailVerificationService;

    private User testUser;
    private VerificationToken testToken;

    @BeforeEach // Setup method runs before each test
    void setUp() {
        // Set the @Value fields manually for the test instance
        ReflectionTestUtils.setField(emailVerificationService, "tokenValidityHours", 24);
        ReflectionTestUtils.setField(emailVerificationService, "frontendUrl", "http://localhost:3000/");

        // Create common test data
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setId(UUID.randomUUID());

        testToken = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
    }

    @Test
    void sendVerificationEmail_ShouldCallEmailService_WithCorrectParameters() {
        // Arrange: Define the behavior of the EmailService mock
        // When emailService.sendEmail is called with any arguments, return an OK response
        when(emailServiceMock.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok("Email sent successfully"));

        // Act: Call the method under test
        ResponseEntity<String> response = emailVerificationService.sendVerificationEmail(testUser, testToken);

        // Assert: Verify the outcomes
        // 1. Check if the response is what we expect (based on the mock)
        assert response.getStatusCode() == HttpStatus.OK;

        // 2. Verify that the emailServiceMock.sendEmail method was called exactly once
        //    with the user's email, a specific subject, and any HTML content string.
        verify(emailServiceMock, times(1)).sendEmail(
                eq(testUser.getEmail()),                  // Check if the first argument is the user's email
                eq("Please verify your email address"),  // Check if the second argument is the correct subject
                anyString()                                // Allow any string for the HTML content
        );
    }

    @Test
    void createVerificationToken_ShouldSaveAndReturnToken() {
        // Arrange: Define behavior for the repository mock
        // When tokenRepository.findByUserAndUsed is called, return empty (no existing token)
        when(tokenRepositoryMock.findByUserAndUsed(any(User.class), eq(false))).thenReturn(java.util.Optional.empty());
        // When tokenRepository.save is called with any VerificationToken, return the same token
        when(tokenRepositoryMock.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Call the method under test
        VerificationToken createdToken = emailVerificationService.createVerificationToken(testUser);

        // Assert: Verify the outcomes
        // 1. Check that the returned token is not null and belongs to the user
        assert createdToken != null;
        assert createdToken.getUser().equals(testUser);
        assert !createdToken.isUsed();
        // 2. Verify that save was called on the repository exactly once
        verify(tokenRepositoryMock, times(1)).save(any(VerificationToken.class));
        // 3. Verify findByUserAndUsed was called
        verify(tokenRepositoryMock, times(1)).findByUserAndUsed(eq(testUser), eq(false));
        // 4. Verify delete was NOT called (since findByUserAndUsed returned empty)
        verify(tokenRepositoryMock, never()).delete(any(VerificationToken.class));
    }

    @Test
    void verifyToken_WithValidToken_ShouldMarkTokenUsedAndUserVerified() {
        // Arrange: Define behavior for repository mock
        testUser.setEmailVerified(false); // Ensure user starts as not verified
        testToken.setUsed(false); // Ensure token starts as not used
        when(tokenRepositoryMock.findByToken(testToken.getToken())).thenReturn(java.util.Optional.of(testToken));
        // When save is called, just return the token passed to it
        when(tokenRepositoryMock.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Call the method under test
        boolean result = emailVerificationService.verifyToken(testToken.getToken());

        // Assert: Verify outcomes
        assert result == true; // Verification should succeed
        assert testToken.isUsed() == true; // Token should now be marked as used
        assert testUser.isEmailVerified() == true; // User should now be marked as verified

        // Verify findByToken was called
        verify(tokenRepositoryMock, times(1)).findByToken(testToken.getToken());
        // Verify save was called (to update the token's used status)
        verify(tokenRepositoryMock, times(1)).save(testToken);
    }

     @Test
    void verifyToken_WithInvalidOrExpiredToken_ShouldReturnFalse() {
        // Arrange: Test with a token that doesn't exist
        when(tokenRepositoryMock.findByToken("invalid-token")).thenReturn(java.util.Optional.empty());

        // Arrange: Test with an expired token
        VerificationToken expiredToken = VerificationToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusDays(1)) // Expired yesterday
                .used(false)
                .build();
        when(tokenRepositoryMock.findByToken("expired-token")).thenReturn(java.util.Optional.of(expiredToken));

        // Arrange: Test with an already used token
        VerificationToken usedToken = VerificationToken.builder()
                .token("used-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(1)) // Not expired
                .used(true) // Already used
                .build();
         when(tokenRepositoryMock.findByToken("used-token")).thenReturn(java.util.Optional.of(usedToken));


        // Act & Assert for invalid token
        boolean resultInvalid = emailVerificationService.verifyToken("invalid-token");
        assert resultInvalid == false;

        // Act & Assert for expired token
        boolean resultExpired = emailVerificationService.verifyToken("expired-token");
         assert resultExpired == false;

         // Act & Assert for used token
         boolean resultUsed = emailVerificationService.verifyToken("used-token");
         assert resultUsed == false;

         // Verify save was never called in these failing scenarios
         verify(tokenRepositoryMock, never()).save(any(VerificationToken.class));
    }

} 