package stud.ntnu.krisefikser.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import stud.ntnu.krisefikser.auth.exception.InvalidCredentialsException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.PasswordResetTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
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
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final JwtProperties jwtProperties;
  private final EmailService emailService;

  /**
   * Registers a new user and generates access and refresh tokens.
   *
   * @param registerRequest The registration request containing user details.
   * @return A response containing the access and refresh tokens.
   */
  public RegisterResponse register(RegisterRequest registerRequest) {
    User user = userService.createUser(new CreateUser(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName(),
        true,
        true,
        true));

    UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).user(user).build());

    return new RegisterResponse(
        accessToken,
        refreshToken);
  }

  /**
   * Authenticates a user and generates access and refresh tokens.
   *
   * @param loginRequest The login request containing user credentials.
   * @return A response containing the access and refresh tokens.
   */
  public LoginResponse login(LoginRequest loginRequest) {
    User user = userService.getUserByEmail(loginRequest.getEmail());

    // Check if account is locked
    if (user != null && user.getLockedUntil() != null && LocalDateTime.now()
        .isBefore(user.getLockedUntil())) {
      log.warn("Account locked attempt for user: {}. Locked until: {}", user.getEmail(),
          user.getLockedUntil());
      throw new LockedException("Account is locked until " + user.getLockedUntil());
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

    if (!passwordEncoder.matches(updatePasswordRequest.getPassword(), user.getPassword())) {
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

    // TODO: Send email with reset password link
//    emailService.sendEmail();

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
    if (request.getEmail() == null) {
      throw new InvalidCredentialsException("Email is required");
    }

    if (request.getToken() == null) {
      throw new InvalidCredentialsException("Token is required");
    }

    if (request.getNewPassword() == null) {
      throw new InvalidCredentialsException("New password is required");
    }

    PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(
            request.getToken())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid token"));

    User user = userService.getUserByEmail(request.getEmail());

    if (!passwordResetToken.getUser().getId().equals(user.getId())) {
      throw new InvalidCredentialsException("Invalid token");
    }

    if (passwordResetToken.isExpired()) {
      passwordResetTokenRepository.delete(passwordResetToken);
      throw new InvalidCredentialsException("Expired token");
    }

    userService.updatePassword(user.getId(), request.getNewPassword());
    passwordResetTokenRepository.delete(passwordResetToken);

    return PasswordResetResponse.builder()
        .message("Password updated")
        .success(true)
        .build();
  }
}
