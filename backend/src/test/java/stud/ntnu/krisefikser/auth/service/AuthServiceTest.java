package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
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
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.exception.EmailNotVerifiedException;
import stud.ntnu.krisefikser.auth.exception.InvalidCredentialsException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.auth.exception.TwoFactorAuthRequiredException;
import stud.ntnu.krisefikser.auth.repository.PasswordResetTokenRepository;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.config.FrontendConfig;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.service.EmailVerificationService;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.exception.UserNotFoundException;
import stud.ntnu.krisefikser.user.repository.UserRepository;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private TurnstileService turnstileService;

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

  @Mock
  private EmailVerificationService emailVerificationService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FrontendConfig frontendConfig;

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

    // Mock the user object instead of creating a real one
    user = mock(stud.ntnu.krisefikser.user.entity.User.class);
    when(user.getId()).thenReturn(UUID.randomUUID());
    when(user.getEmail()).thenReturn("test@example.com");
    when(user.getFirstName()).thenReturn("Test");
    when(user.getLastName()).thenReturn("User");
    when(user.getPassword()).thenReturn("encoded-password");
    when(user.isEmailVerified()).thenReturn(true);
    when(user.getPasswordRetries()).thenReturn(0);

    refreshToken = RefreshToken.builder()
        .id(UUID.randomUUID())
        .token("refresh-token-123")
        .build();

    // Configure JWT properties
    when(jwtProperties.getAccessTokenExpiration()).thenReturn(300000L);
    when(jwtProperties.getRefreshTokenExpiration()).thenReturn(3600000L);
    when(jwtProperties.getResetPasswordTokenExpiration()).thenReturn(3600000L);

    // Configure token service
    when(tokenService.generateAccessToken(any(UserDetails.class))).thenReturn("generated-token");
    when(tokenService.generateRefreshToken(any(UserDetails.class))).thenReturn("generated-token");
    when(tokenService.extractEmail(anyString())).thenReturn("test@example.com");
    when(tokenService.isRefreshToken(anyString())).thenReturn(true);
    when(tokenService.isValid(anyString(), any(UserDetails.class))).thenReturn(true);
    when(tokenService.generateResetPasswordToken(anyString())).thenReturn("reset-token");

    // Configure email verification service
    when(emailVerificationService.sendPasswordResetEmail(any(), anyString(), anyLong()))
        .thenReturn(ResponseEntity.ok("Email sent successfully"));

    when(frontendConfig.getUrl()).thenReturn("http://localhost:3000");
  }

  @Test
  void registerAndSendVerificationEmail_ShouldSendVerificationEmail() {
    // Arrange
    when(userService.createUser(any(CreateUser.class))).thenReturn(user);
    when(turnstileService.verify(anyString())).thenReturn(true);

    // Mock the verification token creation
    VerificationToken mockToken = mock(VerificationToken.class);
    when(emailVerificationService.createVerificationToken(
        any(stud.ntnu.krisefikser.user.entity.User.class)))
        .thenReturn(mockToken);

    // Act
    var response = authService.registerAndSendVerificationEmail(registerRequest);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo(
        "User registered successfully. Verification email sent.");
    assertThat(response.isSuccess()).isTrue();

    verify(userService).createUser(any(CreateUser.class));
    verify(emailVerificationService).createVerificationToken(
        any(stud.ntnu.krisefikser.user.entity.User.class));
    verify(emailVerificationService).sendVerificationEmail(
        any(stud.ntnu.krisefikser.user.entity.User.class),
        eq(mockToken));
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  void registerAndSendVerificationEmail_WhenTurnstileFails_ShouldThrowException() {
    // Arrange
    when(turnstileService.verify(anyString())).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> authService.registerAndSendVerificationEmail(registerRequest))
        .isInstanceOf(TurnstileVerificationException.class);

    verify(userService, never()).createUser(any(CreateUser.class));
  }

  @Test
  void registerAdmin_WithValidToken_ShouldCreateAdmin() {
    // Arrange
    RegisterRequest adminRequest = new RegisterRequest(
        "admin@example.com", "password", "Admin", "User", null);

    when(userService.createUser(any(CreateUser.class))).thenReturn(user);

    // Use reflection to set admin token in the map
    java.lang.reflect.Field field;
    try {
      field = AuthService.class.getDeclaredField("adminInviteTokens");
      field.setAccessible(true);
      java.util.Map<String, Object> map = new java.util.HashMap<>();
      // Create token with matching email that's not expired
      java.lang.reflect.Constructor<?> constructor =
          Class.forName("stud.ntnu.krisefikser.auth.service.AuthService$AdminInviteToken")
              .getDeclaredConstructors()[0];
      constructor.setAccessible(true);
      map.put("token",
          constructor.newInstance("admin@example.com", Instant.now().plusSeconds(3600)));
      field.set(authService, map);
    } catch (Exception e) {
      // Ignore for test purposes
    }

    // Act
    var response = authService.registerAdmin(adminRequest);

    // Assert
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo("Admin user registered successfully.");

    // Verify user created with both USER and ADMIN roles
    ArgumentCaptor<CreateUser> createUserCaptor = ArgumentCaptor.forClass(CreateUser.class);
    verify(userService).createUser(createUserCaptor.capture());
    assertThat(createUserCaptor.getValue().getRoles())
        .containsExactlyInAnyOrder(RoleType.USER, RoleType.ADMIN);

    // Verify user's email is set as verified
    verify(user).setEmailVerified(true);
    verify(userRepository).save(user);
  }

  @Test
  void registerAdmin_WithInvalidToken_ShouldThrowException() {
    // Arrange
    RegisterRequest adminRequest = new RegisterRequest(
        "admin@example.com", "password", "Admin", "User", null);

    // Act & Assert
    assertThatThrownBy(() -> authService.registerAdmin(adminRequest))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid admin invite token");

    verify(userService, never()).createUser(any(CreateUser.class));
  }

  @Test
  void login_ShouldAuthenticateAndReturnTokens() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
    when(userService.getUserByEmail(anyString())).thenReturn(user);

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
  void login_WithUnverifiedEmail_ShouldThrowException() {
    // Arrange
    when(userService.getUserByEmail(anyString())).thenReturn(user);
    when(user.isEmailVerified()).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(EmailNotVerifiedException.class)
        .hasMessage("Email address not verified. Please verify your email before logging in.");

    verify(authenticationManager, never()).authenticate(
        any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void login_WithAdminUser_ShouldRequireTwoFactorAuth() {
    // Arrange
    Role adminRole = mock(Role.class);
    when(adminRole.getName()).thenReturn(RoleType.ADMIN);

    List<Role> roles = new ArrayList<>();
    roles.add(adminRole);

    when(userService.getUserByEmail(anyString())).thenReturn(user);
    when(user.getRoles()).thenReturn(new HashSet<>(roles));

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(TwoFactorAuthRequiredException.class);

    verify(emailVerificationService).sendAdminLoginVerificationEmail(eq(user), anyString());
    verify(authenticationManager, never()).authenticate(any());
  }

  @Test
  void login_WithLockedAccount_ShouldThrowException() {
    // Arrange
    when(userService.getUserByEmail(anyString())).thenReturn(user);
    when(user.getLockedUntil()).thenReturn(LocalDateTime.now().plusMinutes(5));

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(LockedException.class)
        .hasMessageContaining("Account is locked until");

    verify(authenticationManager, never()).authenticate(any());
  }

  @Test
  void login_WithFailedAuthentication_ShouldIncrementRetries() {
    // Arrange
    when(userService.getUserByEmail(anyString())).thenReturn(user);
    when(user.getPasswordRetries()).thenReturn(0);
    when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Auth failed"));

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(RuntimeException.class);

    verify(user).setPasswordRetries(1);
    verify(userRepository).save(user);
  }

  @Test
  void login_WithTooManyFailedAttempts_ShouldLockAccount() {
    // Arrange
    when(userService.getUserByEmail(anyString())).thenReturn(user);
    // Return 4 for the first call, then 5 for the second call
    when(user.getPasswordRetries()).thenReturn(4).thenReturn(5);
    when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Auth failed"));

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(RuntimeException.class);

    // Capture the LocalDateTime argument
    ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(user).setPasswordRetries(5);
    verify(user).setLockedUntil(timeCaptor.capture());

    // Verify it's a future time
    assertThat(timeCaptor.getValue()).isAfter(LocalDateTime.now());
    verify(userRepository).save(user);
  }

  @Test
  void refresh_WithValidToken_ShouldReturnNewTokens() {
    // Arrange
    when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
    when(userService.getUserByEmail(anyString())).thenReturn(user);

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
    when(tokenService.isRefreshToken(anyString())).thenReturn(true);
    when(tokenService.extractEmail(anyString())).thenReturn("test@example.com");
    when(tokenService.isValid(anyString(), any())).thenReturn(true);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> authService.refresh(refreshRequest))
        .isInstanceOf(RefreshTokenDoesNotExistException.class);

    verify(refreshTokenRepository).findByToken("refresh-token-123");
  }

  @Test
  void verifyEmail_WhenTokenIsValid_ShouldReturnTrue() {
    // Arrange
    String token = "valid-token";
    when(emailVerificationService.verifyToken(token)).thenReturn(true);

    // Act
    boolean result = authService.verifyEmail(token);

    // Assert
    assertThat(result).isTrue();
    verify(emailVerificationService).verifyToken(token);
  }

  @Test
  void verifyEmail_WhenTokenIsInvalid_ShouldReturnFalse() {
    // Arrange
    String token = "invalid-token";
    when(emailVerificationService.verifyToken(token)).thenReturn(false);

    // Act
    boolean result = authService.verifyEmail(token);

    // Assert
    assertThat(result).isFalse();
    verify(emailVerificationService).verifyToken(token);
  }

  @Test
  void me_ShouldReturnCurrentUser() {
    // Arrange
    UserResponse userResponse = new UserResponse(
        UUID.randomUUID(),
        "test@example.com",
        List.of("USER"),
        "Test",
        "User",
        true,
        true,
        true,
        null,
        null);
    when(userService.getCurrentUser()).thenReturn(user);
    when(user.toDto()).thenReturn(userResponse);

    // Act
    var result = authService.me();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");

    verify(userService).getCurrentUser();
    verify(user).toDto();
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
    when(user.getFirstName()).thenReturn("Test");
    when(passwordResetTokenRepository.findByUser(user)).thenReturn(Collections.emptyList());

    // Act
    PasswordResetResponse response = authService.requestPasswordReset(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).contains("Reset password request sent to");

    verify(userService).getUserByEmail(email);
    verify(passwordResetTokenRepository).findByUser(user);
    verify(passwordResetTokenRepository, never()).deleteAll(any());
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    verify(emailVerificationService).sendPasswordResetEmail(eq(user), anyString(),
        eq(3600000L / (1000 * 60 * 60)));
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
    when(user.getFirstName()).thenReturn("Test");

    // Act
    PasswordResetResponse response = authService.requestPasswordReset(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();

    verify(passwordResetTokenRepository).findByUser(user);
    verify(passwordResetTokenRepository).deleteAll(existingTokens);
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    verify(emailVerificationService).sendPasswordResetEmail(eq(user), anyString(),
        eq(3600000L / (1000 * 60 * 60)));
  }

  @Test
  void completePasswordReset_WithValidToken_ShouldUpdatePasswordAndDeleteToken() {
    // Arrange
    String token = "valid-token";
    String newPassword = "NewPassword123!";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(token, newPassword);

    stud.ntnu.krisefikser.user.entity.User user = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(
        Optional.of(passwordResetToken));
    when(passwordResetToken.getUser()).thenReturn(user);
    when(user.getId()).thenReturn(UUID.randomUUID());
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
  void completePasswordReset_WithMissingToken_ShouldThrowException() {
    // Arrange
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        null, "NewPassword123!");

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
        "token", null);

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("New password is required");

    verify(passwordResetTokenRepository, never()).findByToken(anyString());
  }

  @Test
  void completePasswordReset_WithInvalidToken_ShouldThrowException() {
    // Arrange
    String token = "invalid-token";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        token, "NewPassword123!");

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

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
    String token = "expired-token";
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(token,
        "NewPassword123!");

    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(
        Optional.of(passwordResetToken));
    when(passwordResetToken.isExpired()).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> authService.completePasswordReset(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Expired token");

    verify(passwordResetTokenRepository).findByToken(token);
    verify(passwordResetTokenRepository).delete(passwordResetToken);
    verify(userService, never()).updatePassword(any(UUID.class), anyString());
  }

  @Test
  void generateAdminInviteToken_ShouldReturnToken() {
    // Arrange
    String email = "admin@example.com";

    // Act
    String token = authService.generateAdminInviteToken(email);

    // Assert
    assertThat(token).isNotNull().isNotEmpty();
  }

  @Test
  void verifyAdminInviteToken_WithInvalidToken_ShouldThrowException() {
    // Arrange
    String invalidToken = "invalid-token";

    // Act & Assert
    assertThatThrownBy(() -> authService.verifyAdminInviteToken(invalidToken))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Invalid token");
  }

  @Test
  void verifyAdminInviteToken_WithValidToken_ShouldReturnEmail() {
    // Arrange
    String email = "admin@example.com";
    String token = authService.generateAdminInviteToken(email);

    // Act
    String result = authService.verifyAdminInviteToken(token);

    // Assert
    assertThat(result).isEqualTo(email);
  }

  @Test
  void verifyAdminLogin_WithValidToken_ShouldReturnLoginResponse() {
    // Arrange
    String token = "admin-login-token";
    String email = "admin@example.com";

    Role adminRole = mock(Role.class);
    when(adminRole.getName()).thenReturn(RoleType.ADMIN);

    Set<Role> roles = new HashSet<>();
    roles.add(adminRole);

    stud.ntnu.krisefikser.user.entity.User adminUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    when(adminUser.getRoles()).thenReturn(roles);

    when(tokenService.extractEmail(token)).thenReturn(email);
    when(userService.getUserByEmail(email)).thenReturn(adminUser);
    when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

    // Act
    LoginResponse response = authService.verifyAdminLogin(token);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isEqualTo("generated-token");
    assertThat(response.getRefreshToken()).isEqualTo("generated-token");

    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void verifyAdminLogin_WithInvalidToken_ShouldThrowException() {
    // Arrange
    String token = "invalid-token";
    when(tokenService.extractEmail(token)).thenReturn(null);

    // Act & Assert
    assertThatThrownBy(() -> authService.verifyAdminLogin(token))
        .isInstanceOf(InvalidTokenException.class);

    verify(userService, never()).getUserByEmail(anyString());
  }

  @Test
  void verifyAdminLogin_WithNonAdminUser_ShouldThrowException() {
    // Arrange
    String token = "user-token";
    String email = "user@example.com";

    stud.ntnu.krisefikser.user.entity.User regularUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    Role userRole = mock(Role.class);
    when(userRole.getName()).thenReturn(RoleType.USER);

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
    when(regularUser.getRoles()).thenReturn(roles);

    when(tokenService.extractEmail(token)).thenReturn(email);
    when(userService.getUserByEmail(email)).thenReturn(regularUser);

    // Act & Assert
    assertThatThrownBy(() -> authService.verifyAdminLogin(token))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void requestAdminPasswordReset_WithValidAdminUser_ShouldGenerateAndSendResetToken() {
    // Arrange
    String adminEmail = "admin@example.com";
    RequestPasswordResetRequest request = new RequestPasswordResetRequest(adminEmail);

    stud.ntnu.krisefikser.user.entity.User adminUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    Role adminRole = mock(Role.class);
    when(adminRole.getName()).thenReturn(RoleType.ADMIN);

    Set<Role> roles = new HashSet<>();
    roles.add(adminRole);
    when(adminUser.getRoles()).thenReturn(roles);

    when(userService.getUserByEmail(adminEmail)).thenReturn(adminUser);
    when(adminUser.getEmail()).thenReturn(adminEmail);
    when(adminUser.getFirstName()).thenReturn("Admin");

    // Act
    PasswordResetResponse response = authService.requestAdminPasswordReset(request);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).contains("Reset password request sent to");

    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    verify(emailVerificationService).sendPasswordResetEmail(eq(adminUser), anyString(), anyLong());
  }

  @Test
  void requestAdminPasswordReset_WithNonAdminUser_ShouldThrowException() {
    // Arrange
    String userEmail = "user@example.com";
    RequestPasswordResetRequest request = new RequestPasswordResetRequest(userEmail);

    stud.ntnu.krisefikser.user.entity.User regularUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    Role userRole = mock(Role.class);
    when(userRole.getName()).thenReturn(RoleType.USER);

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
    when(regularUser.getRoles()).thenReturn(roles);

    when(userService.getUserByEmail(userEmail)).thenReturn(regularUser);

    // Act & Assert
    assertThatThrownBy(() -> authService.requestAdminPasswordReset(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("User is not an admin or is a superadmin");
  }

  @Test
  void requestAdminPasswordReset_WithSuperAdminUser_ShouldThrowException() {
    // Arrange
    String superAdminEmail = "superadmin@example.com";
    RequestPasswordResetRequest request = new RequestPasswordResetRequest(superAdminEmail);

    stud.ntnu.krisefikser.user.entity.User superAdminUser = mock(
        stud.ntnu.krisefikser.user.entity.User.class);
    Role adminRole = mock(Role.class);
    when(adminRole.getName()).thenReturn(RoleType.ADMIN);

    Role superAdminRole = mock(Role.class);
    when(superAdminRole.getName()).thenReturn(RoleType.SUPER_ADMIN);

    Set<Role> roles = new HashSet<>();
    roles.add(adminRole);
    roles.add(superAdminRole);
    when(superAdminUser.getRoles()).thenReturn(roles);

    when(userService.getUserByEmail(superAdminEmail)).thenReturn(superAdminUser);

    // Act & Assert
    assertThatThrownBy(() -> authService.requestAdminPasswordReset(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("User is not an admin or is a superadmin");
  }

  @Test
  void requestAdminPasswordReset_WithNonExistingUser_ShouldThrowException() {
    // Arrange
    String email = "nonexistent@example.com";
    RequestPasswordResetRequest request = new RequestPasswordResetRequest(email);
    when(userService.getUserByEmail(email)).thenThrow(
        new UserNotFoundException("User with email " + email + " does not exist"));

    // Act & Assert
    assertThatThrownBy(() -> authService.requestAdminPasswordReset(request))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(email);
  }

  @Test
  void isValidAdminInviteToken_WithValidToken_ShouldReturnTrue() {
    // Arrange
    String email = "admin@example.com";
    authService.generateAdminInviteToken(email);

    // Act
    boolean result = authService.isValidAdminInviteToken(email);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void isValidAdminInviteToken_WithInvalidEmail_ShouldReturnFalse() {
    // Arrange
    String email = "nonexisting@example.com";

    // Act
    boolean result = authService.isValidAdminInviteToken(email);

    // Assert
    assertThat(result).isFalse();
  }
}