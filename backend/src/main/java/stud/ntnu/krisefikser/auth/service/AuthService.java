package stud.ntnu.krisefikser.auth.service;

import jakarta.transaction.Transactional;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import stud.ntnu.krisefikser.auth.exception.EmailNotVerifiedException;
import stud.ntnu.krisefikser.user.service.UserService;
import stud.ntnu.krisefikser.email.service.EmailVerificationService;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import java.time.LocalDateTime;

/**
 * Service class for handling authentication-related operations such as user
 * registration, login,
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

if (!user.isEmailVerified()) {
  throw new EmailNotVerifiedException("Email address not verified. Please verify your email before logging in.");
}

    // Check if account is locked
    if (user != null && user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
      log.warn("Account locked attempt for user: {}. Locked until: {}", user.getEmail(), user.getLockedUntil());
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
}
