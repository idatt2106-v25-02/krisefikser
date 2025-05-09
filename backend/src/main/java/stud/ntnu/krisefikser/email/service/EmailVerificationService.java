package stud.ntnu.krisefikser.email.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.config.FrontendConfig;
import stud.ntnu.krisefikser.email.config.MailProperties;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Service class for handling email verification tokens and sending verification emails.
 *
 * <p>This service is responsible for creating, verifying, and sending email verification tokens to
 * users. It also handles the sending of password reset emails and admin login verification
 * emails.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

  private final VerificationTokenRepository tokenRepository;
  private final EmailService emailService;
  private final MailProperties mailProperties;
  private final EmailTemplateService emailTemplateService;
  private final FrontendConfig frontendConfig;

  /**
   * Creates a new verification token for the specified user.
   *
   * <p>This method deletes any existing unused tokens for the user before creating a new one.</p>
   *
   * @param user the user for whom the verification token is created
   * @return the newly created verification token
   */
  @Transactional
  public VerificationToken createVerificationToken(User user) {
    // Delete any existing unused tokens for this user
    Optional<VerificationToken> existingToken = tokenRepository.findByUserAndUsed(user, false);
    existingToken.ifPresent(tokenRepository::delete);

    // Create new token
    VerificationToken token = VerificationToken.builder()
        .token(UUID.randomUUID().toString())
        .user(user)
        .expiryDate(
            LocalDateTime.now().plusHours(mailProperties.getVerificationTokenValidityHours()))
        .used(false)
        .build();

    return tokenRepository.save(token);
  }

  /**
   * Verifies the provided token and marks it as used if valid.
   *
   * <p>This method checks if the token is valid, not used, and not expired. If valid, it marks the
   * token as used and updates the user's email verification status.</p>
   *
   * @param token the token to verify
   * @return true if the token is valid and successfully verified, false otherwise
   */
  @Transactional
  public boolean verifyToken(String token) {
    Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);

    if (verificationToken.isEmpty() || verificationToken.get().isUsed() || verificationToken.get()
        .isExpired()) {
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
   * Sends a verification email to the specified user.
   *
   * <p>This method constructs the verification email content and sends it using the email service.
   * It includes a link to verify the user's email address.</p>
   *
   * @param user  the user to whom the verification email is sent
   * @param token the verification token to include in the email
   * @return the response entity containing the email sending result
   */
  public ResponseEntity<String> sendVerificationEmail(User user, VerificationToken token) {
    String verificationLink = frontendConfig.getUrl() + "/verify?token=" + token.getToken();
    String htmlContent = createVerificationEmailHtml(user.getFirstName(), verificationLink);

    return emailService.sendEmail(
        user.getEmail(),
        "Please verify your email address",
        htmlContent
    );
  }

  private String createVerificationEmailHtml(String firstName, String verificationLink) {
    return "<html><body>"
        + "<h2>Welcome to Krisefikser!</h2>"
        + "<p>Hello " + firstName + ",</p>"
        + "<p>Thank you for registering. Please click the link below to verify your email "
        + "address:</p>"
        + "<p><a href='" + verificationLink + "'>Verify Email Address</a></p>"
        + "<p>This link will expire in " + mailProperties.getVerificationTokenValidityHours()
        + " hours.</p>"
        + "<p>If you did not create this account, you can safely ignore this email.</p>"
        + "</body></html>";
  }

  /**
   * Sends a password reset email to the specified user.
   *
   * @param user            the user requesting the password reset
   * @param resetLink       the password reset link
   * @param expirationHours the number of hours until the reset link expires
   */
  public ResponseEntity<String> sendPasswordResetEmail(User user, String resetLink,
      long expirationHours) {
    String htmlContent = createPasswordResetEmailHtml(user.getFirstName(), resetLink,
        expirationHours);

    return emailService.sendEmail(
        user.getEmail(),
        "Reset your password",
        htmlContent
    );
  }

  private String createPasswordResetEmailHtml(String firstName, String resetLink,
      long expirationHours) {
    return "<html><body>"
        + "<h2>Reset Your Password</h2>"
        + "<p>Hello " + firstName + ",</p>"
        + "<p>We received a request to reset your password. Click the link below to reset it:</p>"
        + "<p><a href='" + resetLink + "'>Reset Password</a></p>"
        + "<p>This link will expire in " + expirationHours + " hours.</p>"
        + "<p>If you did not request a password reset, you can safely ignore this email.</p>"
        + "</body></html>";
  }

  /**
   * Sends an admin login verification email to the specified user.
   *
   * @param user             the admin user attempting to log in
   * @param verificationLink the verification link for admin login
   */
  public ResponseEntity<String> sendAdminLoginVerificationEmail(User user,
      String verificationLink) {
    String htmlContent = createAdminLoginVerificationEmailHtml(user.getFirstName(),
        verificationLink);

    return emailService.sendEmail(
        user.getEmail(),
        "Admin Login Verification",
        htmlContent
    );
  }

  private String createAdminLoginVerificationEmailHtml(String firstName, String verificationLink) {
    return "<html><body>"
        + "<h2>Admin Login Verification</h2>"
        + "<p>Hello " + firstName + ",</p>"
        + "<p>We detected an admin login attempt. To complete the login process, please click the "
        + "link below:</p>"
        + "<p><a href='" + verificationLink + "'>Verify Admin Login</a></p>"
        + "<p>This link will expire in " + mailProperties.getVerificationTokenValidityHours()
        + " hours.</p>"
        + "<p>If you did not attempt to log in, please contact the system administrator immediately"
        + ".</p>"
        + "</body></html>";
  }

  /**
   * Sends a password change notification email to the user.
   *
   * @param user            The user whose password was changed
   * @param resetLink       The password reset link in case the change was unauthorized
   * @param expirationHours The number of hours until the reset link expires
   * @return A ResponseEntity containing the response from the email service
   */
  public ResponseEntity<String> sendPasswordChangeNotification(User user, String resetLink,
      long expirationHours) {
    try {
      log.info("Preparing password change notification email for user: {}", user.getEmail());

      Map<String, String> variables = new HashMap<>();
      variables.put("firstName", user.getFirstName());
      variables.put("resetLink", resetLink);
      variables.put("expirationHours", String.valueOf(expirationHours));

      log.debug("Loading email template with variables: {}", variables);
      String content =
          emailTemplateService.loadAndReplace("password-change-notification.html", variables);

      log.info("Sending password change notification email to: {}", user.getEmail());
      ResponseEntity<String> response = emailService.sendEmail(
          user.getEmail(),
          "Password Change Notification",
          content
      );

      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Successfully sent password change notification email to: {}", user.getEmail());
      } else {
        log.error(
            "Failed to send password change notification email to: {}. Status: {}, Response: {}",
            user.getEmail(), response.getStatusCode(), response.getBody());
      }

      return response;
    } catch (Exception e) {
      log.error("Error sending password change notification email to: {}. Error: {}",
          user.getEmail(), e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to send password change notification: " + e.getMessage());
    }
  }
}