package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.auth.config.JwtProperties;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private JwtProperties jwtProperties;

  @Mock
  private TokenService tokenService;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks
  private AuthService authService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private RefreshRequest refreshRequest;
  private UserDetails userDetails;
  private stud.ntnu.krisefikser.user.entity.User user;
  private RefreshToken refreshToken;

  @BeforeEach
  void setUp() {
    registerRequest = new RegisterRequest("test@example.com", "password", "Test", "User",
        "turnstile-token");
    loginRequest = new LoginRequest("test@example.com", "password");
    refreshRequest = new RefreshRequest("refresh-token-123");

    userDetails = User.builder()
        .username("test@example.com")
        .password("encoded-password")
        .roles("USER")
        .build();

    Role role = new Role();
    role.setName(Role.RoleType.USER);

    user = stud.ntnu.krisefikser.user.entity.User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .firstName("Test")
        .lastName("User")
        .password("encoded-password")
        .roles(Collections.singleton(role))
        .build();

    refreshToken = RefreshToken.builder()
        .id(UUID.randomUUID())
        .token("refresh-token-123")
        .build();

    // Configure JWT properties
    when(jwtProperties.getAccessTokenExpiration()).thenReturn(300000L);
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(3600000L);

    // Configure token service
    when(tokenService.generateAccessToken(any(UserDetails.class))).thenReturn("generated-token");
    when(tokenService.generateRefreshToken(any(UserDetails.class))).thenReturn("generated-token");
    when(tokenService.extractEmail(anyString())).thenReturn("test@example.com");
  }

  @Test
  void register_ShouldReturnTokens() {
    // Arrange
    when(userService.createUser(any(CreateUser.class))).thenReturn(user);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

    // Act
    var response = authService.register(registerRequest);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("generated-token");
    assertThat(response.getRefreshToken()).isEqualTo("generated-token");

    verify(userService).createUser(any(CreateUser.class));
    verify(userDetailsService).loadUserByUsername("test@example.com");
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void login_ShouldAuthenticateAndReturnTokens() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

    // Act
    LoginResponse response = authService.login(loginRequest);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("generated-token");
    assertThat(response.getRefreshToken()).isEqualTo("generated-token");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userDetailsService).loadUserByUsername("test@example.com");
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void refresh_WithValidToken_ShouldReturnNewTokens() {
    // Arrange
    when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

    // Act
    var response = authService.refresh(refreshRequest);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("generated-token");
    assertThat(response.getRefreshToken()).isEqualTo("generated-token");

    verify(refreshTokenRepository).findByToken("refresh-token-123");
    verify(refreshTokenRepository).delete(refreshToken);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void refresh_WithInvalidToken_ShouldThrowException() {
    // Arrange
    when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> authService.refresh(refreshRequest))
        .isInstanceOf(RefreshTokenDoesNotExistException.class);

    verify(refreshTokenRepository).findByToken("refresh-token-123");
    verify(refreshTokenRepository, never()).delete(any());
    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  void me_ShouldReturnCurrentUser() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(user);

    // Act
    var result = authService.me();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");

    verify(userService).getCurrentUser();
  }
}