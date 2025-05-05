package stud.ntnu.krisefikser.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
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
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()));

    UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

    User user = userService.getUserByEmail(loginRequest.getEmail());

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
