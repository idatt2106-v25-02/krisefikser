package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import stud.ntnu.krisefikser.auth.config.JwtProperties;
import stud.ntnu.krisefikser.auth.dto.CompletePasswordResetRequest;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.PasswordResetResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RequestPasswordResetRequest;
import stud.ntnu.krisefikser.auth.dto.UpdatePasswordRequest;
import stud.ntnu.krisefikser.auth.dto.UpdatePasswordResponse;
import stud.ntnu.krisefikser.auth.entity.PasswordResetToken;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.exception.InvalidCredentialsException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.PasswordResetTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

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

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

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

  @Test
  void updatePassword_WhenPasswordValid_ShouldUpdateUserPassword() {
    // Arrange
    String oldPassword = "oldPassword123";
    String newPassword = "newPassword456";
    UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(oldPassword,
        newPassword);
    stud.ntnu.krisefikser.user.entity.User currentUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(currentUser.getId()).thenReturn(UUID.randomUUID());
    when(currentUser.getPassword()).thenReturn("encoded-old-password");
    when(passwordEncoder.matches(oldPassword, "encoded-old-password")).thenReturn(true);

    // Act
    UpdatePasswordResponse response = authService.updatePassword(updatePasswordRequest);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo("Password updated");

    verify(userService).getCurrentUser();
    verify(userService).updatePassword(any(UUID.class), eq(newPassword));
  }

  @Test
  void updatePassword_WhenPasswordInvalid_ShouldThrowException() {
    // Arrange
    String oldPassword = "wrongPassword";
    String newPassword = "newPassword456";
    UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(oldPassword,
        newPassword);
    stud.ntnu.krisefikser.user.entity.User currentUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(currentUser.getPassword()).thenReturn("encoded-password");
    when(passwordEncoder.matches(oldPassword, "encoded-password")).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> authService.updatePassword(updatePasswordRequest))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid password");

    verify(userService).getCurrentUser();
    verify(userService, never()).updatePassword(any(UUID.class), anyString());
  }

  @Test
  void requestPasswordReset_ShouldGenerateAndSaveToken() {
    // Arrange
    String email = "test@example.com";
    RequestPasswordResetRequest request = new RequestPasswordResetRequest(email);
    stud.ntnu.krisefikser.user.entity.User user = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    when(userService.getUserByEmail(email)).thenReturn(user);
    when(tokenService.generateResetPasswordToken(email)).thenReturn("reset-token");
    when(jwtProperties.getResetPasswordTokenExpiration()).thenReturn(3600000L);
    when(user.getEmail()).thenReturn(email);

    // Act
    PasswordResetResponse response = authService.requestPasswordReset(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).contains("Reset password request sent to");

    verify(userService).getUserByEmail(email);
    verify(passwordResetTokenRepository).findByUser(user);
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
  }

  @Test
  void requestPasswordReset_WithExistingTokens_ShouldDeleteOldTokensFirst() {
    // Arrange
    String email = "test@example.com";
    RequestPasswordResetRequest request = new RequestPasswordResetRequest(email);
    stud.ntnu.krisefikser.user.entity.User user = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    List<PasswordResetToken> existingTokens = List.of(mock(PasswordResetToken.class));

    when(userService.getUserByEmail(email)).thenReturn(user);
    when(passwordResetTokenRepository.findByUser(user)).thenReturn(existingTokens);
    when(tokenService.generateResetPasswordToken(email)).thenReturn("reset-token");
    when(jwtProperties.getResetPasswordTokenExpiration()).thenReturn(3600000L);
    when(user.getEmail()).thenReturn(email);

    // Act
    PasswordResetResponse response = authService.requestPasswordReset(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();

    verify(passwordResetTokenRepository).findByUser(user);
    verify(passwordResetTokenRepository).deleteAll(existingTokens);
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
  }

  @Test
  void completePasswordReset_WithValidToken_ShouldUpdatePasswordAndDeleteToken() {
    // Arrange
    String email = "test@example.com";
    String token = "valid-token";
    String newPassword = "NewPassword123!";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(email, token,
        newPassword);

    stud.ntnu.krisefikser.user.entity.User user = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(
        Optional.of(passwordResetToken));
    when(userService.getUserByEmail(email)).thenReturn(user);
    when(user.getId()).thenReturn(UUID.randomUUID());
    when(passwordResetToken.getUser()).thenReturn(user);
    when(passwordResetToken.isExpired()).thenReturn(false);

    // Act
    PasswordResetResponse response = authService.completePasswordReset(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo("Password updated");

    verify(userService).updatePassword(any(UUID.class), eq(newPassword));
    verify(passwordResetTokenRepository).delete(passwordResetToken);
  }

  @Test
  void completePasswordReset_WithMissingEmail_ShouldThrowException() {
    // Arrange
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        null, "token", "NewPassword123!");

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Email is required");

    verify(passwordResetTokenRepository, never()).findByToken(anyString());
  }

  @Test
  void completePasswordReset_WithMissingToken_ShouldThrowException() {
    // Arrange
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        "test@example.com", null, "NewPassword123!");

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Token is required");

    verify(passwordResetTokenRepository, never()).findByToken(anyString());
  }

  @Test
  void completePasswordReset_WithMissingPassword_ShouldThrowException() {
    // Arrange
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        "test@example.com", "token", null);

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("New password is required");

    verify(passwordResetTokenRepository, never()).findByToken(anyString());
  }

  @Test
  void completePasswordReset_WithInvalidToken_ShouldThrowException() {
    // Arrange
    String email = "test@example.com";
    String token = "invalid-token";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        email, token, "NewPassword123!");

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid token");

    verify(passwordResetTokenRepository).findByToken(token);
    verify(userService, never()).updatePassword(any(UUID.class), anyString());
  }

  @Test
  void completePasswordReset_WithMismatchedUser_ShouldThrowException() {
    // Arrange
    String email = "test@example.com";
    String token = "valid-token";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        email, token, "NewPassword123!");

    stud.ntnu.krisefikser.user.entity.User requestUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    stud.ntnu.krisefikser.user.entity.User tokenUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(
        Optional.of(passwordResetToken));
    when(userService.getUserByEmail(email)).thenReturn(requestUser);
    when(passwordResetToken.getUser()).thenReturn(tokenUser);
    when(requestUser.getId()).thenReturn(UUID.randomUUID());
    when(tokenUser.getId()).thenReturn(UUID.randomUUID()); // Different IDs

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid token");

    verify(passwordResetTokenRepository).findByToken(token);
    verify(userService, never()).updatePassword(any(UUID.class), anyString());
  }

  @Test
  void completePasswordReset_WithExpiredToken_ShouldThrowException() {
    // Arrange
    String email = "test@example.com";
    String token = "expired-token";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        email, token, "NewPassword123!");

    stud.ntnu.krisefikser.user.entity.User user = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(
        Optional.of(passwordResetToken));
    when(userService.getUserByEmail(email)).thenReturn(user);
    when(user.getId()).thenReturn(UUID.randomUUID());
    when(passwordResetToken.getUser()).thenReturn(user);
    when(passwordResetToken.isExpired()).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Expired token");

    verify(passwordResetTokenRepository).findByToken(token);
    verify(passwordResetTokenRepository).delete(passwordResetToken);
    verify(userService, never()).updatePassword(any(UUID.class), anyString());
  }
}