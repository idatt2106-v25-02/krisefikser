package stud.ntnu.krisefikser.auth.service;

import java.util.Date;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.config.JwtProperties;
import stud.ntnu.krisefikser.auth.dto.*;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.email.service.EmailVerificationService;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service class for handling authentication-related operations such as registration, login,
 * token generation, refresh, and email verification.
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
  private final EmailVerificationService emailVerificationService;

  /**
   * Registers a new user and generates access and refresh tokens.
   *
   * @param registerRequest The registration request.
   * @return A response with tokens.
   */
  public RegisterResponse register(RegisterRequest registerRequest) {
    userService.createUser(new CreateUser(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName(),
        true, true, true));

    UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());

    String accessToken = createAccessToken(userDetails);
    String refreshToken = createRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new RegisterResponse(accessToken, refreshToken);
  }

  /**
   * Authenticates a user and returns new access and refresh tokens.
   *
   * @param loginRequest The login credentials.
   * @return A response with tokens.
   */
  public LoginResponse login(LoginRequest loginRequest) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()));

    UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

    String accessToken = createAccessToken(userDetails);
    String refreshToken = createRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new LoginResponse(accessToken, refreshToken);
  }

  /**
   * Refreshes an access token using a valid refresh token.
   *
   * @param refreshRequest The request containing refresh token.
   * @return A response with new tokens.
   */
  public RefreshResponse refresh(RefreshRequest refreshRequest) {
    RefreshToken existingToken = refreshTokenRepository.findByToken(refreshRequest.getRefreshToken())
        .orElseThrow(RefreshTokenDoesNotExistException::new);

    String email = tokenService.extractEmail(existingToken.getToken());
    if (email == null) throw new InvalidTokenException();

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    String accessToken = createAccessToken(userDetails);
    String refreshToken = createRefreshToken(userDetails);

    refreshTokenRepository.delete(existingToken);
    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new RefreshResponse(accessToken, refreshToken);
  }

  /**
   * Verifies an email token sent to the user.
   *
   * @param token Email verification token.
   * @return true if verification is successful.
   */
  @Transactional
  public boolean verifyEmail(String token) {
    boolean verified = emailVerificationService.verifyToken(token);
    if (verified) {
      log.info("Email verified successfully with token: {}", token);
    } else {
      log.warn("Failed verification attempt with token: {}", token);
    }
    return verified;
  }

  /**
   * Gets current authenticated user.
   *
   * @return User details.
   */
  public UserResponse me() {
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
