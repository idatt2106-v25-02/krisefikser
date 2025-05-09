package stud.ntnu.krisefikser.email.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

  private final VerificationTokenRepository tokenRepository;
  private final EmailService emailService;
  private final EmailTemplateService emailTemplateService;

  @Value("${verification.token.validity-hours:24}")
  private int tokenValidityHours;

  @Value("${frontend.url}")
  private String frontendUrl;

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

  public ResponseEntity<String> sendVerificationEmail(User user, VerificationToken token) {
    try {
      String verificationLink = frontendUrl + "/verify?token=" + token.getToken();

      Map<String, String> variables = new HashMap<>();
      variables.put("name", user.getFirstName());
      variables.put("link", verificationLink);

      String htmlContent = emailTemplateService.loadAndReplace("verification.html", variables);

      return emailService.sendEmail(
          user.getEmail(),
          "Please verify your email address",
          htmlContent
      );
    } catch (IOException e) {
      log.error("Error sending verification email to: {}. Error: {}",
          user.getEmail(), e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to send verification email: " + e.getMessage());
    }
  }

  public ResponseEntity<String> sendPasswordResetEmail(User user, String resetLink,
      long expirationHours) {
    try {
      Map<String, String> variables = new HashMap<>();
      variables.put("name", user.getFirstName());
      variables.put("link", resetLink);

      String htmlContent = emailTemplateService.loadAndReplace("password-reset.html", variables);

      return emailService.sendEmail(
          user.getEmail(),
          "Reset your password",
          htmlContent
      );
    } catch (IOException e) {
      log.error("Error sending password reset email to: {}. Error: {}",
          user.getEmail(), e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to send password reset email: " + e.getMessage());
    }
  }

  public ResponseEntity<String> sendAdminLoginVerificationEmail(User user,
      String verificationLink) {
    try {
      Map<String, String> variables = new HashMap<>();
      variables.put("name", user.getFirstName());
      variables.put("link", verificationLink);

      String htmlContent = emailTemplateService.loadAndReplace("admin-login-verification.html", variables);

      return emailService.sendEmail(
          user.getEmail(),
          "Admin Login Verification",
          htmlContent
      );
    } catch (IOException e) {
      log.error("Error sending admin login verification email to: {}. Error: {}",
          user.getEmail(), e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to send admin login verification email: " + e.getMessage());
    }
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
