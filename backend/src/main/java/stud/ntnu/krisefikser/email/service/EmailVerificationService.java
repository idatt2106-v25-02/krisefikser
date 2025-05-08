package stud.ntnu.krisefikser.email.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

  private final VerificationTokenRepository tokenRepository;
  private final EmailService emailService;

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
    String verificationLink = frontendUrl + "/verify?token=" + token.getToken();
    String htmlContent = createVerificationEmailHtml(user.getFirstName(), verificationLink);

    return emailService.sendEmail(
        user.getEmail(),
        "Please verify your email address",
        htmlContent
    );
  }

  private String createVerificationEmailHtml(String firstName, String verificationLink) {
    return "<html><body>" +
        "<h2>Welcome to Krisefikser!</h2>" +
        "<p>Hello " + firstName + ",</p>" +
        "<p>Thank you for registering. Please click the link below to verify your email address:</p>"
        +
        "<p><a href='" + verificationLink + "'>Verify Email Address</a></p>" +
        "<p>This link will expire in " + tokenValidityHours + " hours.</p>" +
        "<p>If you did not create this account, you can safely ignore this email.</p>" +
        "</body></html>";
  }

  /**
   * Sends a password reset email to the specified user.
   *
   * @param user            the user requesting the password reset
   * @param resetLink       the password reset link
   * @param expirationHours the number of hours until the reset link expires
   * @return a ResponseEntity containing the response from the email service
   */
  public ResponseEntity<String> sendPasswordResetEmail(User user, String resetLink,
      long expirationHours) {
    String htmlContent =
        createPasswordResetEmailHtml(user.getFirstName(), resetLink, expirationHours);

    return emailService.sendEmail(
        user.getEmail(),
        "Reset your password",
        htmlContent
    );
  }

  private String createPasswordResetEmailHtml(String firstName, String resetLink,
      long expirationHours) {
    return "<html><body>" +
        "<h2>Reset Your Password</h2>" +
        "<p>Hello " + firstName + ",</p>" +
        "<p>We received a request to reset your password. Click the link below to reset it:</p>" +
        "<p><a href='" + resetLink + "'>Reset Password</a></p>" +
        "<p>This link will expire in " + expirationHours + " hours.</p>" +
        "<p>If you did not request a password reset, you can safely ignore this email.</p>" +
        "</body></html>";
  }

  /**
   * Sends an admin login verification email to the specified user.
   *
   * @param user             the admin user attempting to log in
   * @param verificationLink the verification link for admin login
   * @return a ResponseEntity containing the response from the email service
   */
  public ResponseEntity<String> sendAdminLoginVerificationEmail(User user,
      String verificationLink) {
    String htmlContent =
        createAdminLoginVerificationEmailHtml(user.getFirstName(), verificationLink);

    return emailService.sendEmail(
        user.getEmail(),
        "Admin Login Verification",
        htmlContent
    );
  }

  private String createAdminLoginVerificationEmailHtml(String firstName, String verificationLink) {
    return "<html><body>" +
        "<h2>Admin Login Verification</h2>" +
        "<p>Hello " + firstName + ",</p>" +
        "<p>We detected an admin login attempt. To complete the login process, please click the link below:</p>"
        +
        "<p><a href='" + verificationLink + "'>Verify Admin Login</a></p>" +
        "<p>This link will expire in " + tokenValidityHours + " hours.</p>" +
        "<p>If you did not attempt to log in, please contact the system administrator immediately.</p>"
        +
        "</body></html>";
  }
}