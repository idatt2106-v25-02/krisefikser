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

  // Rest of the class remains unchanged
  public LoginResponse login(LoginRequest loginRequest) {
    // existing implementation
  }

  public RefreshResponse refresh(RefreshRequest refreshRequest) {
    // existing implementation
  }

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