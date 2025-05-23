package stud.ntnu.krisefikser.auth.service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.config.JwtProperties;
import stud.ntnu.krisefikser.auth.dto.CompletePasswordResetRequest;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.PasswordResetResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.dto.RequestPasswordResetRequest;
import stud.ntnu.krisefikser.auth.dto.UpdatePasswordRequest;
import stud.ntnu.krisefikser.auth.dto.UpdatePasswordResponse;
import stud.ntnu.krisefikser.auth.entity.PasswordResetToken;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.exception.EmailNotVerifiedException;
import stud.ntnu.krisefikser.auth.exception.InvalidCredentialsException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.auth.exception.TwoFactorAuthRequiredException;
import stud.ntnu.krisefikser.auth.repository.PasswordResetTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.config.FrontendConfig;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.service.EmailVerificationService;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.exception.UserNotFoundException;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service class for handling authentication-related operations such as user registration, login,
 * token generation, and token refresh.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final CustomUserDetailsService userDetailsService;
  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenRepository refreshTokenRepository;
  private final EmailVerificationService emailVerificationService;
  private final UserRepository userRepository;
  private final TurnstileService turnstileService;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final JwtProperties jwtProperties;
  private final Map<String, AdminInviteToken> adminInviteTokens = new ConcurrentHashMap<>();
  private final FrontendConfig frontendConfig;

  /**
   * Registers a new user and sends verification email.
   *
   * @param registerRequest The registration request containing user details.
   * @return A response containing the access and refresh tokens.
   */
  @Transactional
  public RegisterResponse registerAndSendVerificationEmail(RegisterRequest registerRequest) {
    validateTurnstileToken(registerRequest.getTurnstileToken());

    User user = userService.createUser(new CreateUser(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName(),
        true,
        true,
        true,
        List.of(RoleType.USER)
    ));

    // Send verification email, should throw if error occurs, which will be caught by the
    // transaction
    VerificationToken token = emailVerificationService.createVerificationToken(user);
    emailVerificationService.sendVerificationEmail(user, token);

    return new RegisterResponse("User registered successfully. Verification email sent.", true);
  }

  private void validateTurnstileToken(String turnstileToken) throws TurnstileVerificationException {
    boolean isHuman = turnstileService.verify(turnstileToken);
    if (!isHuman) {
      throw new TurnstileVerificationException();
    }
  }

  /**
   * Registers a new admin user with both USER and ADMIN roles.
   *
   * @param request The registration request containing admin user details.
   * @return A response containing the access and refresh tokens.
   */
  @Transactional
  public RegisterResponse registerAdmin(RegisterRequest request) {
    if (!isValidAdminInviteToken(request.getEmail())) {
      throw new InvalidCredentialsException("Invalid admin invite token");
    }

    User user = userService.createUser(new CreateUser(
        request.getEmail(),
        request.getPassword(),
        request.getFirstName(),
        request.getLastName(),
        true,
        true,
        true,
        List.of(RoleType.USER, RoleType.ADMIN)
    ));

    // Set email as verified for admins
    user.setEmailVerified(true);
    userRepository.save(user);

    return new RegisterResponse("Admin user registered successfully.", true);
  }

  /**
   * Checks if an email has a valid admin invite token.
   *
   * @param email The email to check
   * @return true if the email has a valid invite token, false otherwise
   */
  public boolean isValidAdminInviteToken(String email) {
    return adminInviteTokens.values().stream()
        .anyMatch(token -> token.email.equals(email) && !token.isExpired());
  }

  /**
   * Authenticates a user and generates access and refresh tokens.
   *
   * @param loginRequest The login request containing user credentials.
   * @return A response containing the access and refresh tokens.
   */
  @Transactional
  public LoginResponse login(LoginRequest loginRequest) {
    User user = userService.getUserByEmail(loginRequest.getEmail());
    if (user == null) {
      log.warn("Login attempt for non-existing user: {}", loginRequest.getEmail());
      throw new UserNotFoundException("User not found");
    }
    if (!user.isEmailVerified()) {
      throw new EmailNotVerifiedException(
          "Email address not verified. Please verify your email before logging in.");
    }

    // Check if account is locked
    if (user.getLockedUntil() != null && LocalDateTime.now()
        .isBefore(user.getLockedUntil())) {
      log.warn("Account locked attempt for user: {}. Locked until: {}", user.getEmail(),
          user.getLockedUntil());
      throw new LockedException("Account is locked until " + user.getLockedUntil());
    }

    if (user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(RoleType.ADMIN))
        && user.getRoles().stream()
        .noneMatch(role -> role.getName().equals(RoleType.SUPER_ADMIN))) {
      log.info("Admin login attempt for user: {}", user.getEmail());

      // Generate verification token for admin login
      String token = tokenService.generateResetPasswordToken(user.getEmail());
      String verificationLink = frontendConfig.getUrl() + "/verify-admin-login?token=" + token;

      // Send verification email
      emailVerificationService.sendAdminLoginVerificationEmail(user, verificationLink);

      throw new TwoFactorAuthRequiredException();
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getEmail(),
              loginRequest.getPassword()));

      // Reset password retries on successful login
      if (user.getPasswordRetries() > 0) {
        user.setPasswordRetries(0);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        userRepository.save(user);
      }
    } catch (Exception e) {
      // Handle failed login attempts
      user.setPasswordRetries(user.getPasswordRetries() + 1);

      // Lock account after 5 failed attempts
      if (user.getPasswordRetries() >= 5) {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        log.info("Account locked for user: {} until {}", user.getEmail(), user.getLockedUntil());
      }

      userRepository.save(user);
      throw e;
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new LoginResponse(
        accessToken,
        refreshToken);
  }

  /**
   * Refreshes the access token using the provided refresh token.
   *
   * @param refreshRequest The request containing the refresh token.
   * @return A response containing the new access and refresh tokens.
   */
  @Transactional
  public RefreshResponse refresh(RefreshRequest refreshRequest) {
    if (!tokenService.isRefreshToken(refreshRequest.getRefreshToken())) {
      throw new InvalidTokenException();
    }

    String email = tokenService.extractEmail(refreshRequest.getRefreshToken());
    if (email == null) {
      throw new InvalidTokenException();
    }

    log.info("Refreshing token");

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    if (!tokenService.isValid(refreshRequest.getRefreshToken(), userDetails)) {
      throw new InvalidTokenException();
    }

    RefreshToken existingToken = refreshTokenRepository.findByToken(
        refreshRequest.getRefreshToken()).orElseThrow(
        RefreshTokenDoesNotExistException::new);

    User user = userService.getUserByEmail(email);

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.delete(existingToken);
    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new RefreshResponse(
        accessToken,
        refreshToken);
  }

  /**
   * Verifies the email address using the provided token.
   *
   * @param token The verification token.
   * @return true if the email is verified, false otherwise.
   */
  @Transactional
  public boolean verifyEmail(String token) {
    boolean verified = emailVerificationService.verifyToken(token);
    if (verified) {
      // Any additional action after verification
      log.info("Email verified successfully with token: {}", token);
    } else {
      log.warn("Failed verification attempt with token: {}", token);
    }
    return verified;
  }

  /**
   * Retrieves the currently authenticated user's details.
   *
   * @return UserResponse containing the user's details.
   */
  public UserResponse me() {
    return userService.getCurrentUser().toDto();
  }

  /**
   * Updates the password of the currently authenticated user.
   *
   * @param updatePasswordRequest The request containing the new password.
   * @return UpdatePasswordResponse containing the status of the update.
   */
  @Transactional
  public UpdatePasswordResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
    User user = userService.getCurrentUser();
    log.info("Starting password update process for user: {}", user.getEmail());

    // Verify current password
    if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getPassword())) {
      log.warn("Password update failed - incorrect current password for user: {}", user.getEmail());
      throw new InvalidCredentialsException("Current password is incorrect");
    }

    // Update password
    log.info("Updating password for user: {}", user.getEmail());
    userService.updatePassword(user.getId(), updatePasswordRequest.getPassword());

    // Generate reset token for security
    log.info("Generating reset token for user: {}", user.getEmail());
    String token = tokenService
        .generateResetPasswordToken(user.getEmail());
    long expirationHours = jwtProperties.getResetPasswordTokenExpiration() / (1000 * 60 * 60);

    // Save token
    PasswordResetToken passwordResetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiryDate(Instant.now().plus(Duration.ofHours(expirationHours)))
        .build();

    passwordResetTokenRepository.save(passwordResetToken);
    log.info("Reset token saved for user: {}", user.getEmail());

    // Send notification email with reset link
    String resetLink = frontendConfig.getUrl() + "/verifiser-passord-tilbakestilling?token="
        + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
    log.info("Sending password change notification email to: {}", user.getEmail());
    ResponseEntity<String> emailResponse = emailVerificationService.sendPasswordChangeNotification(
        user, resetLink, expirationHours);

    if (emailResponse.getStatusCode().is2xxSuccessful()) {
      log.info("Password change notification email sent successfully to: {}", user.getEmail());
    } else {
      log.error(
          "Failed to send password change notification email to: {}. Status: {}, Response: {}",
          user.getEmail(), emailResponse.getStatusCode(), emailResponse.getBody());
    }

    return new UpdatePasswordResponse("Password updated successfully", true);
  }

  /**
   * Requests a password reset by generating a reset token and sending it to the user's email.
   *
   * @param request The request containing the user's email.
   * @return A response indicating the success of the operation.
   */
  @Transactional
  public PasswordResetResponse requestPasswordReset(
      RequestPasswordResetRequest request) {
    User user = userService.getUserByEmail(request.getEmail());

    List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findByUser(user);

    if (!existingTokens.isEmpty()) {
      passwordResetTokenRepository.deleteAll(existingTokens);
    }

    String token = tokenService.generateResetPasswordToken(user.getEmail());

    PasswordResetToken passwordResetToken = PasswordResetToken.builder().token(token).user(user)
        .expiryDate(
            Instant.now().plusMillis(jwtProperties.getResetPasswordTokenExpiration()))
        .build();

    passwordResetTokenRepository.save(passwordResetToken);

    // Send password reset email
    String resetLink =
        frontendConfig.getUrl() + "/verifiser-passord-tilbakestilling?token="
            + java.net.URLEncoder.encode(
            token, java.nio.charset.StandardCharsets.UTF_8);
    long expirationHours = jwtProperties.getResetPasswordTokenExpiration() / (1000 * 60 * 60);

    emailVerificationService.sendPasswordResetEmail(
        user,
        resetLink,
        expirationHours
    );

    return PasswordResetResponse.builder()
        .message("Reset password request sent to " + user.getEmail())
        .success(true)
        .build();
  }

  /**
   * Completes the password reset process by validating the token and updating the user's password.
   *
   * @param request The request containing the token and new password.
   * @return A response indicating the success of the operation.
   */
  @Transactional
  public PasswordResetResponse completePasswordReset(CompletePasswordResetRequest request) {
    if (request.getToken() == null) {
      throw new InvalidCredentialsException("Token is required");
    }

    if (request.getNewPassword() == null) {
      throw new InvalidCredentialsException("New password is required");
    }

    PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(
            request.getToken())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid token"));

    if (passwordResetToken.isExpired()) {
      passwordResetTokenRepository.delete(passwordResetToken);
      throw new InvalidCredentialsException("Expired token");
    }

    User user = passwordResetToken.getUser();
    userService.updatePassword(user.getId(), request.getNewPassword());
    passwordResetTokenRepository.delete(passwordResetToken);

    return PasswordResetResponse.builder()
        .message("Password updated")
        .success(true)
        .build();
  }

  /**
   * Generates a unique token for admin invitation.
   *
   * @param email The email address of the invited user
   * @return A unique token for the admin invitation
   */
  public String generateAdminInviteToken(String email) {
    String token = UUID.randomUUID().toString();
    Instant expiryDate = Instant.now().plus(Duration.ofHours(24));

    adminInviteTokens.put(token, new AdminInviteToken(email, expiryDate));

    return token;
  }

  /**
   * Verifies an admin invitation token and returns the associated email address.
   *
   * @param token The invitation token to verify
   * @return The email address associated with the token
   * @throws RuntimeException if token is invalid or expired
   */
  public String verifyAdminInviteToken(String token) {
    AdminInviteToken inviteToken = adminInviteTokens.get(token);

    if (inviteToken == null) {
      throw new InvalidTokenException("Invalid token");
    }

    if (inviteToken.isExpired()) {
      adminInviteTokens.remove(token);
      throw new InvalidTokenException("Token has expired");
    }

    return inviteToken.email;
  }

  /**
   * Verifies an admin login attempt using the provided token.
   *
   * @param token The verification token sent to the admin's email
   * @return A LoginResponse containing the access and refresh tokens if verification is successful
   * @throws InvalidTokenException if the token is invalid or expired
   */
  @Transactional
  public LoginResponse verifyAdminLogin(String token) {
    String email = tokenService.extractEmail(token);
    if (email == null) {
      throw new InvalidTokenException();
    }

    User user = userService.getUserByEmail(email);
    if (user == null || user.getRoles().stream()
        .noneMatch(role -> role.getName().equals(RoleType.ADMIN))
        && user.getRoles().stream()
        .noneMatch(role -> role.getName().equals(RoleType.SUPER_ADMIN))) {
      throw new InvalidTokenException();
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new LoginResponse(accessToken, refreshToken);
  }

  /**
   * Requests a password reset for an admin user. This method is specifically for superadmins to
   * initiate password resets for admin users.
   *
   * @param request The request containing the admin's email address
   * @return A response indicating the success of the operation
   */
  @Transactional
  public PasswordResetResponse requestAdminPasswordReset(RequestPasswordResetRequest request) {
    User user = userService.getUserByEmail(request.getEmail());
    if (user == null) {
      throw new UserNotFoundException("User not found");
    }

    // Verify that the user is an admin but not a superadmin
    boolean isAdmin = user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(RoleType.ADMIN));
    boolean isSuperAdmin = user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(RoleType.SUPER_ADMIN));

    if (!isAdmin || isSuperAdmin) {
      throw new RuntimeException("User is not an admin or is a superadmin");
    }

    // Generate reset token
    String token = tokenService
        .generateResetPasswordToken(user.getEmail());
    long expirationHours = jwtProperties.getResetPasswordTokenExpiration() / (1000 * 60 * 60);

    // Save token
    PasswordResetToken passwordResetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiryDate(Instant.now().plus(Duration.ofHours(expirationHours)))
        .build();

    passwordResetTokenRepository.save(passwordResetToken);

    // Send email
    String resetLink =
        frontendConfig.getUrl() + "/verifiser-passord-tilbakestilling?token="
            + java.net.URLEncoder.encode(
            token, java.nio.charset.StandardCharsets.UTF_8);
    emailVerificationService.sendPasswordResetEmail(user, resetLink, expirationHours);

    return PasswordResetResponse.builder()
        .message("Reset password request sent to " + user.getEmail())
        .success(true)
        .build();
  }

  private record AdminInviteToken(String email, Instant expiryDate) {

    public boolean isExpired() {
      return Instant.now().isAfter(expiryDate);
    }
  }
}
