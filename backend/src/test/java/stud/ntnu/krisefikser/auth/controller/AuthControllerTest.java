package stud.ntnu.krisefikser.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.service.AuthService;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @MockBean
  private TokenService tokenService;

  @MockBean
  private CustomUserDetailsService userDetailsService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private RefreshRequest refreshRequest;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    registerRequest = new RegisterRequest("test@example.com", "password", "Test", "User", "turnstile-token");
    loginRequest = new LoginRequest("test@example.com", "password");
    refreshRequest = new RefreshRequest("refresh-token-123");
    userResponse = new UserResponse(UUID.randomUUID(), "test@example.com", List.of("USER"), "Test",
        "User",
        false, false, false);
  }

  @Test
  void register_ShouldReturnOkWithTokens() throws Exception {
    // Arrange
    RegisterResponse response = new RegisterResponse("access-token", "refresh-token");
    when(authService.register(any(RegisterRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
  }

  @Test
  void login_ShouldReturnOkWithTokens() throws Exception {
    // Arrange
    LoginResponse response = new LoginResponse("access-token", "refresh-token");
    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
  }

  @Test
  void refresh_ShouldReturnOkWithNewTokens() throws Exception {
    // Arrange
    RefreshResponse response = new RefreshResponse("new-access-token", "new-refresh-token");
    when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
  }

  @Test
  @WithMockUser(username = "test@example.com", roles = "USER")
  void me_ShouldReturnUserDetails() throws Exception {
    // Arrange
    when(authService.me()).thenReturn(userResponse);

    // Act & Assert
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.roles[0]").value("USER"))
        .andExpect(jsonPath("$.firstName").value("Test"))
        .andExpect(jsonPath("$.lastName").value("User"));
  }

  @Test
  void me_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }
}