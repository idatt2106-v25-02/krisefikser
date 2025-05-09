package stud.ntnu.krisefikser.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.dto.AdminInviteRequest;
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
import stud.ntnu.krisefikser.auth.exception.InvalidCredentialsException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.auth.service.AuthService;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.email.service.EmailAdminService;
import stud.ntnu.krisefikser.email.service.EmailVerificationService;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UserNotFoundException;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @MockitoBean
  private TurnstileService turnstileService;

  @MockitoBean
  private EmailVerificationService emailVerificationService;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private EmailAdminService emailAdminService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private RefreshRequest refreshRequest;
  private UserResponse userResponse;
  private User testUser;
  private AdminInviteRequest adminInviteRequest;

  @BeforeEach
  void setUp() {
    registerRequest =
        new RegisterRequest("test@example.com", "Password123!", "Test", "User", "turnstile-token");
    loginRequest = new LoginRequest("test@example.com", "Password123!");
    refreshRequest = new RefreshRequest("refresh-token-123");
    userResponse = new UserResponse(
        UUID.randomUUID(),
        "test@example.com",
        List.of("USER"),
        "Test",
        "User",
        false,
        false,
        false,
        null,
        null);
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    adminInviteRequest = new AdminInviteRequest("admin@example.com");
  }

  @Test
  void register_WithValidTurnstileToken_ShouldReturnOkWithTokens()
      throws Exception {
    RegisterResponse response = new RegisterResponse(
        "User registered successfully. Verification email sent.", true);
    when(turnstileService.verify(any(String.class))).thenReturn(true);
    when(authService.registerAndSendVerificationEmail(any(RegisterRequest.class))).thenReturn(
        response);
    when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));

    mockMvc.perform(post("/api/auth/register")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message").value("User registered successfully. Verification email sent."))
        .andExpect(jsonPath("$.success").value(true));
  }


  @Test
  void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
    when(turnstileService.verify(any(String.class))).thenReturn(true);
    when(authService.registerAndSendVerificationEmail(any(RegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException("Email already exists"));

    mockMvc.perform(post("/api/auth/register")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser
  void verifyEmail_WithValidToken_ShouldReturnOk() throws Exception {
    when(authService.verifyEmail(anyString())).thenReturn(true);

    mockMvc.perform(post("/api/auth/verify-email")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .param("token", "valid-token"))
        .andExpect(status().isOk())
        .andExpect(content().string("Email verified successfully."));
  }

  @Test
  @WithMockUser
  void verifyEmail_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
    when(authService.verifyEmail(anyString())).thenReturn(false);

    mockMvc.perform(post("/api/auth/verify-email")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .param("token", "invalid-token"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid or expired verification token."));
  }

  @Test
  void login_WithValidCredentials_ShouldReturnOkWithTokens() throws Exception {
    LoginResponse response = new LoginResponse("access-token", "refresh-token");
    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/login")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
  }

  @Test
  void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new InvalidCredentialsException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_WithLockedAccount_ShouldReturnLocked() throws Exception {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new LockedException("Account is locked"));

    mockMvc.perform(post("/api/auth/login")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isLocked());
  }

  @Test
  void refresh_WithValidToken_ShouldReturnNewTokens() throws Exception {
    RefreshResponse response = new RefreshResponse("new-access-token", "new-refresh-token");
    when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/refresh")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
  }

  @Test
  void refresh_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
    when(authService.refresh(any(RefreshRequest.class)))
        .thenThrow(new InvalidTokenException());

    mockMvc.perform(post("/api/auth/refresh")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "test@example.com", roles = "USER")
  void me_WithValidToken_ShouldReturnUserDetails() throws Exception {
    when(authService.me()).thenReturn(userResponse);

    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.roles[0]").value("USER"))
        .andExpect(jsonPath("$.firstName").value("Test"))
        .andExpect(jsonPath("$.lastName").value("User"));
  }

  @Test
  void me_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void updatePassword_WithValidPasswords_ShouldReturnSuccess() throws Exception {
    UpdatePasswordRequest request = new UpdatePasswordRequest("OldPassword123!", "NewPassword123!");
    UpdatePasswordResponse response = UpdatePasswordResponse.builder()
        .message("Password updated successfully")
        .success(true)
        .build();
    when(authService.updatePassword(any(UpdatePasswordRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/update-password")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Password updated successfully"));
  }

  @Test
  @WithMockUser
  void updatePassword_WithInvalidCurrentPassword_ShouldReturnUnauthorized() throws Exception {
    UpdatePasswordRequest request =
        new UpdatePasswordRequest("WrongPassword123!", "NewPassword123!");
    when(authService.updatePassword(any(UpdatePasswordRequest.class)))
        .thenThrow(new InvalidCredentialsException("Invalid current password"));

    mockMvc.perform(post("/api/auth/update-password")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void requestPasswordReset_WithValidEmail_ShouldReturnSuccess() throws Exception {
    RequestPasswordResetRequest request = new RequestPasswordResetRequest("test@example.com");
    PasswordResetResponse response = PasswordResetResponse.builder()
        .message("Password reset email sent")
        .success(true)
        .build();
    when(authService.requestPasswordReset(any(RequestPasswordResetRequest.class))).thenReturn(
        response);

    mockMvc.perform(post("/api/auth/request-password-reset")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Password reset email sent"));
  }

  @Test
  void requestPasswordReset_WithNonexistentEmail_ShouldReturnNotFound() throws Exception {
    RequestPasswordResetRequest request =
        new RequestPasswordResetRequest("nonexistent@example.com");
    when(authService.requestPasswordReset(any(RequestPasswordResetRequest.class)))
        .thenThrow(new UserNotFoundException("User not found"));

    mockMvc.perform(post("/api/auth/request-password-reset")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void completePasswordReset_WithValidToken_ShouldReturnSuccess() throws Exception {
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        "valid-token",
        "NewPassword123!");
    PasswordResetResponse response = PasswordResetResponse.builder()
        .message("Password reset successful")
        .success(true)
        .build();
    when(authService.completePasswordReset(any(CompletePasswordResetRequest.class))).thenReturn(
        response);

    mockMvc.perform(post("/api/auth/complete-password-reset")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Password reset successful"));
  }

  @Test
  void completePasswordReset_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        "invalid-token",
        "NewPassword123!");
    when(authService.completePasswordReset(any(CompletePasswordResetRequest.class)))
        .thenThrow(new InvalidTokenException());

    mockMvc.perform(post("/api/auth/complete-password-reset")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void completePasswordReset_WithNonexistentEmail_ShouldReturnNotFound() throws Exception {
    CompletePasswordResetRequest request = new CompletePasswordResetRequest(
        "valid-token",
        "NewPassword123!");
    when(authService.completePasswordReset(any(CompletePasswordResetRequest.class)))
        .thenThrow(new UserNotFoundException("User not found"));

    mockMvc.perform(post("/api/auth/complete-password-reset")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "SUPER_ADMIN")
  void inviteAdmin_WithValidEmail_ShouldReturnOk() throws Exception {
    String inviteToken = "valid-invite-token";
    when(authService.generateAdminInviteToken(anyString())).thenReturn(inviteToken);
    when(emailAdminService.sendAdminInvitation(anyString(), anyString()))
        .thenReturn(ResponseEntity.ok("Admin invitation sent successfully."));

    mockMvc.perform(post("/api/auth/invite/admin")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminInviteRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin invitation sent successfully."));

    verify(authService).generateAdminInviteToken(adminInviteRequest.getEmail());
    verify(emailAdminService).sendAdminInvitation(eq(adminInviteRequest.getEmail()), anyString());
  }

  @Test
  @WithMockUser(roles = "USER")
  void inviteAdmin_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/auth/invite/admin")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminInviteRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void verifyAdminInvite_WithValidToken_ShouldReturnEmail() throws Exception {
    String email = "admin@example.com";
    when(authService.verifyAdminInviteToken(anyString())).thenReturn(email);

    mockMvc.perform(get("/api/auth/verify-admin-invite")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .param("token", "valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void verifyAdminInvite_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
    when(authService.verifyAdminInviteToken(anyString()))
        .thenThrow(new RuntimeException("Invalid token"));

    mockMvc.perform(get("/api/auth/verify-admin-invite")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .param("token", "invalid-token"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void registerAdmin_WithValidData_ShouldReturnOk() throws Exception {
    RegisterResponse response = new RegisterResponse(
        "User registered successfully. Verification email sent.", true);
    when(authService.registerAdmin(any(RegisterRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/register/admin")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message").value("User registered successfully. Verification email sent."))
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @WithMockUser(roles = "USER")
  void registerAdmin_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/auth/register/admin")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  void register_WithInvalidTurnstileToken_ShouldReturnBadRequest() throws Exception {
    // Arrange
    when(turnstileService.verify(any())).thenReturn(false);
    when(authService.registerAndSendVerificationEmail(any(RegisterRequest.class)))
        .thenThrow(new TurnstileVerificationException());

    RegisterRequest registerRequest = new RegisterRequest(
        "newuser@example.com",
        "password123",
        "New",
        "User",
        "invalid-turnstile-token"
    );

    // Act & Assert
    mockMvc.perform(post("/api/auth/register")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest());
  }
}
