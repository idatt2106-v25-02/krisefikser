package stud.ntnu.krisefikser.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class responsible for handling email verification functionality.
 * This service manages the creation, verification, and sending of verification tokens
 * for user email verification and password reset processes.
 *
 * @author Krisefikser Team
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    /**
     * The number of hours for which a verification token remains valid.
     * Default value is 24 hours if not specified in properties.
     */
    @Value("${verification.token.validity-hours:24}")
    private int tokenValidityHours;
    
    /**
     * The base URL of the frontend application.
     * Used to construct verification and password reset links.
     */
    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Creates a new verification token for a user.
     * If an existing unused token exists for the user, it will be deleted before creating a new one.
     *
     * @param user the user for whom to create the verification token
     * @return the newly created VerificationToken
     */
    public VerificationToken createVerificationToken(User user) {
        // Delete any existing unused tokens for this user
        Optional<VerificationToken> existingToken = tokenRepository.findByUserAndUsed(user, false);
        existingToken.ifPresent(tokenRepository::delete);
        
        // Create new token
        VerificationToken token = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(tokenValidityHours))
                .used(false)
                .build();
        
        return tokenRepository.save(token);
    }

    /**
     * Verifies a token and marks the associated user's email as verified if valid.
     * A token is considered valid if it exists, hasn't been used, and hasn't expired.
     *
     * @param token the verification token to validate
     * @return true if the token is valid and verification was successful, false otherwise
     */
    public boolean verifyToken(String token) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);
        
        if (verificationToken.isEmpty() || verificationToken.get().isUsed() || verificationToken.get().isExpired()) {
            return false;
        }
        
        // Mark token as used
        VerificationToken vt = verificationToken.get();
        vt.setUsed(true);
        tokenRepository.save(vt);
        
        // Mark user as verified
        User user = vt.getUser();
        user.setEmailVerified(true);
        
        return true;
    }
    
    /**
     * Sends a verification email to the user with a link to verify their email address.
     *
     * @param user the user to send the verification email to
     * @param token the verification token to include in the email
     * @return a ResponseEntity containing the response from the email service
     */
    public ResponseEntity<String> sendVerificationEmail(User user, VerificationToken token) {
        String verificationLink = frontendUrl + "/verify?token=" + token.getToken();
        String htmlContent = createVerificationEmailHtml(user.getFirstName(), verificationLink);
        
        return emailService.sendEmail(
            user.getEmail(),
            "Please verify your email address",
            htmlContent
        );
    }
    
    /**
     * Creates the HTML content for the verification email.
     *
     * @param firstName the user's first name for personalization
     * @param verificationLink the link to verify the email address
     * @return the HTML content for the verification email
     */
    private String createVerificationEmailHtml(String firstName, String verificationLink) {
        return "<html><body>" +
               "<h2>Welcome to Krisefikser!</h2>" +
               "<p>Hello " + firstName + ",</p>" +
               "<p>Thank you for registering. Please click the link below to verify your email address:</p>" +
               "<p><a href='" + verificationLink + "'>Verify Email Address</a></p>" +
               "<p>This link will expire in " + tokenValidityHours + " hours.</p>" +
               "<p>If you did not create this account, you can safely ignore this email.</p>" +
               "</body></html>";
    }

    /**
     * Sends a password reset email to the specified user.
     *
     * @param user the user requesting the password reset
     * @param resetLink the password reset link
     * @param expirationHours the number of hours until the reset link expires
     * @return a ResponseEntity containing the response from the email service
     */
    public ResponseEntity<String> sendPasswordResetEmail(User user, String resetLink, long expirationHours) {
        String htmlContent = createPasswordResetEmailHtml(user.getFirstName(), resetLink, expirationHours);
        
        return emailService.sendEmail(
            user.getEmail(),
            "Reset your password",
            htmlContent
        );
    }

    /**
     * Creates the HTML content for the password reset email.
     *
     * @param firstName the user's first name for personalization
     * @param resetLink the link to reset the password
     * @param expirationHours the number of hours until the reset link expires
     * @return the HTML content for the password reset email
     */
    private String createPasswordResetEmailHtml(String firstName, String resetLink, long expirationHours) {
        return "<html><body>" +
               "<h2>Reset Your Password</h2>" +
               "<p>Hello " + firstName + ",</p>" +
               "<p>We received a request to reset your password. Click the link below to reset it:</p>" +
               "<p><a href='" + resetLink + "'>Reset Password</a></p>" +
               "<p>This link will expire in " + expirationHours + " hours.</p>" +
               "<p>If you did not request a password reset, you can safely ignore this email.</p>" +
               "</body></html>";
    }
}