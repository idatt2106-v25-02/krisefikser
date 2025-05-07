package stud.ntnu.krisefikser.auth.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
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
  private final TurnstileService turnstileService;
  private final RoleRepository roleRepository;

  /**
   * Registers a new admin user and generates access and refresh tokens.
   *
   * @param request The registration request containing admin user details.
   * @return A response containing the access and refresh tokens.
   */
  public RegisterResponse registerAdmin(RegisterRequest request) {
    // Validate email has permission to register as admin
    // TODO: Implement email validation logic, if fails, throw org.springframework.security.access.AccessDeniedException
    return registerWithRole(request, RoleType.ADMIN);
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
   * Registers a new user and generates access and refresh tokens.
   *
   * @param registerRequest The registration request containing user details.
   * @return A response containing the access and refresh tokens.
   */
  public RegisterResponse register(RegisterRequest registerRequest) {
    return registerWithRole(registerRequest, RoleType.USER);
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
}
