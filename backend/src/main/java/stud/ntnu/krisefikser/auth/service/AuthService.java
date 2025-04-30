package stud.ntnu.krisefikser.auth.service;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.config.JwtProperties;
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
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.user.dto.CreateUserDto;
import stud.ntnu.krisefikser.user.dto.UserDto;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service responsible for handling authentication processes like registration, login, and token refreshing.
 * It interacts with {@link UserService} to manage user data, {@link CustomUserDetailsService}
 * for loading user details during authentication, and {@link EmailService} for sending relevant emails.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final CustomUserDetailsService userDetailsService;
  private final JwtProperties jwtProperties;
  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenRepository refreshTokenRepository;
  private final EmailService emailService; // Added EmailService

  /**
   * Registers a new user.
   * Creates the user via {@link UserService}, then loads the user details to generate authentication tokens.
   * Retrieves the created {@link User} entity using {@link UserService} to send a welcome email via {@link EmailService}.
   *
   * @param registerRequest DTO containing registration details.
   * @return RegisterResponse containing access and refresh tokens.
   */
  public RegisterResponse register(RegisterRequest registerRequest) {
    // Create user and capture the result
    UserDto createdUserDto = userService.createUser(new CreateUserDto(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName()
    ));

    // Get UserDetails for token generation
    UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());

    // Get User entity for email service
    User user = userService.getUserByEmail(registerRequest.getEmail());

    // Send welcome email with verification link
    emailService.sendWelcomeEmail(user, userDetails);

    // Continue with normal registration flow
    String accessToken = createAccessToken(userDetails);
    String refreshToken = createRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new RegisterResponse(
        accessToken,
        refreshToken
    );
  }

  /**
   * Handles user login.
   * Authenticates the user using Spring Security's {@link AuthenticationManager}.
   * Loads user details via {@link CustomUserDetailsService} (which likely uses {@link UserService})
   * to generate access and refresh tokens.
   *
   * @param loginRequest DTO containing login credentials.
   * @return LoginResponse containing access and refresh tokens.
   */
  public LoginResponse login(LoginRequest loginRequest) {
    // Authenticate user
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
    );

    // Load user details and generate tokens
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
    String accessToken = createAccessToken(userDetails);
    String refreshToken = createRefreshToken(userDetails);

    // Save refresh token
    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    // Return response
    return new LoginResponse(accessToken, refreshToken);
  }

  /**
   * Refreshes the authentication tokens using a valid refresh token.
   * Validates the provided refresh token and extracts user details.
   * Uses {@link CustomUserDetailsService} (likely interacting with {@link UserService}) to load user details.
   * Generates new access and refresh tokens.
   *
   * @param refreshRequest DTO containing the refresh token.
   * @return RefreshResponse containing new access and refresh tokens.
   */
  public RefreshResponse refresh(RefreshRequest refreshRequest) {
    // Validate refresh token exists
    String token = refreshRequest.getRefreshToken();
    if (!refreshTokenRepository.existsByToken(token)) {
      throw new RefreshTokenDoesNotExistException("Refresh token does not exist");
    }

    // Verify and extract user details
    String email = tokenService.extractUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    // Validate token
    if (!tokenService.validate(token, userDetails)) {
      throw new InvalidTokenException("Invalid refresh token");
    }

    // Generate new tokens
    String accessToken = createAccessToken(userDetails);
    String newRefreshToken = createRefreshToken(userDetails);

    // Update refresh token in repository
    refreshTokenRepository.delete(refreshTokenRepository.findByToken(token).get());
    refreshTokenRepository.save(RefreshToken.builder().token(newRefreshToken).build());

    return new RefreshResponse(accessToken, newRefreshToken);
  }

  /**
   * Retrieves the details of the currently authenticated user.
   * Delegates the retrieval to {@link UserService#getCurrentUser()}.
   *
   * @return UserDto representing the current user.
   */
  public UserDto me() {
    return userService.getCurrentUser().toDto();
  }

  private String createAccessToken(UserDetails userDetails) {
    return tokenService.generate(userDetails, getAccessTokenExpiration());
  }

  private String createRefreshToken(UserDetails userDetails) {
    return tokenService.generate(userDetails, getRefreshTokenExpiration());
  }

  private Date getAccessTokenExpiration() {
    return new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration());
  }

  private Date getRefreshTokenExpiration() {
    return new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration());
  }
}