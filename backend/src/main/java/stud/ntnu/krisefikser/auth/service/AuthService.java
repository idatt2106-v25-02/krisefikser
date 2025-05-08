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
import org.springframework.beans.factory.annotation.Value;
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
import stud.ntnu.krisefikser.email.service.EmailService;
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
  private final EmailService emailService;
  private final Map<String, AdminInviteToken> adminInviteTokens = new ConcurrentHashMap<>();
  @Value("${frontend.url}")
  private String frontendUrl;

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
   * Registers a new admin user and generates access and refresh tokens.
   *
   * @param request The registration request containing admin user details.
   * @return A response containing the access and refresh tokens.
   */
  public RegisterResponse registerAdmin(RegisterRequest request) {
    // Validate email has permission to register as admin
    // TODO: Implement email validation logic, if fails, throw org.springframework.security.access.AccessDeniedException
    User user = userService.createUser(new CreateUser(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            true,  // enabled
            true,  // emailVerified
            true), // accountNonLocked
        RoleType.ADMIN);

    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new RegisterResponse(
        accessToken,
        refreshToken);
  }

  /**
   * Registers a new user and generates access and refresh tokens.
   *
   * @param registerRequest The registration request containing user details.
   * @return A response containing the access and refresh tokens.
   */
  public RegisterResponse register(RegisterRequest registerRequest) {
    return registerWithRole(registerRequest, RoleType.USER);
  }

  private RegisterResponse registerWithRole(RegisterRequest registerRequest, RoleType roleType) {
    validateTurnstileToken(registerRequest.getTurnstileToken());

    User user = userService.createUser(new CreateUser(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName(),
        true,
        true,
        true), roleType);

    UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new RegisterResponse(
        accessToken,
        refreshToken);
  }

  private void validateTurnstileToken(String turnstileToken) throws TurnstileVerificationException {
    boolean isHuman = turnstileService.verify(turnstileToken);
    if (!isHuman) {
      throw new TurnstileVerificationException();
    }
  }

  /**
   * Authenticates a user and generates access and refresh tokens.
   *
   * @param loginRequest The login request containing user credentials.
   * @return A response containing the access and refresh tokens.
   */
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
        .anyMatch(role -> role.getName().equals(RoleType.ADMIN)) && 
        !user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleType.SUPER_ADMIN))) {
      log.info("Admin login attempt for user: {}", user.getEmail());

      // Generate verification token for admin login
      String token = tokenService.generateResetPasswordToken(user.getEmail());
      String verificationLink = frontendUrl + "/verify-admin-login?token=" + token;

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
      if (user != null && user.getPasswordRetries() > 0) {
        user.setPasswordRetries(0);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // Set to past time
        userRepository.save(user);
      }
    } catch (Exception e) {
      // Handle failed login attempts
      user.setPasswordRetries(user.getPasswordRetries() + 1);

      // Lock account after 5 failed attempts
      if (user != null && user.getPasswordRetries() >= 5) {
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
  public RefreshResponse refresh(RefreshRequest refreshRequest) {
    RefreshToken existingToken = refreshTokenRepository.findByToken(
        refreshRequest.getRefreshToken()).orElseThrow(
        RefreshTokenDoesNotExistException::new);

    String email = tokenService.extractEmail(existingToken.getToken());
    if (email == null) {
      throw new InvalidTokenException();
    }

    log.info("Refreshing token");

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    User user = userService.getUserByEmail(email);

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.delete(existingToken);
    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new RefreshResponse(
        accessToken,
        refreshToken);
  }

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
  public UpdatePasswordResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
    User user = userService.getCurrentUser();

    if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("Invalid password");
    }

    userService.updatePassword(user.getId(), updatePasswordRequest.getPassword());

    return UpdatePasswordResponse.builder()
        .message("Password updated")
        .success(true)
        .build();
  }

  /**
   * Requests a password reset by generating a reset token and sending it to the user's email.
   *
   * @param request The request containing the user's email.
   * @return A response indicating the success of the operation.
   */
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
    String resetLink = frontendUrl + "/verifiser-passord-tilbakestilling?token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
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
      throw new RuntimeException("Invalid token");
    }

    if (inviteToken.isExpired()) {
      adminInviteTokens.remove(token);
      throw new RuntimeException("Token has expired");
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
  public LoginResponse verifyAdminLogin(String token) {
    String email = tokenService.extractEmail(token);
    if (email == null) {
      throw new InvalidTokenException();
    }

    User user = userService.getUserByEmail(email);
    if (user == null || !user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(RoleType.ADMIN))) {
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
    String token = tokenService.generateResetPasswordToken(user.getEmail());
    long expirationHours = jwtProperties.getResetPasswordTokenExpiration() / (1000 * 60 * 60);

    // Save token
    PasswordResetToken passwordResetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiryDate(Instant.now().plus(Duration.ofHours(expirationHours)))
        .build();

    passwordResetTokenRepository.save(passwordResetToken);

    // Send email
    String resetLink = frontendUrl + "/verifiser-passord-tilbakestilling?token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
    emailVerificationService.sendPasswordResetEmail(user, resetLink, expirationHours);

    return PasswordResetResponse.builder()
        .message("Reset password request sent to " + user.getEmail())
        .success(true)
        .build();
  }

  private static class AdminInviteToken {

    private final String email;
    private final Instant expiryDate;

    public AdminInviteToken(String email, Instant expiryDate) {
      this.email = email;
      this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
      return Instant.now().isAfter(expiryDate);
    }
  }
}
