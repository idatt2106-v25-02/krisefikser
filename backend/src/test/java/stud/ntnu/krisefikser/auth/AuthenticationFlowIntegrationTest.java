package stud.ntnu.krisefikser.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @MockitoBean
  private TurnstileService turnstileService;

  @MockitoBean
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    // Mock Turnstile verification to always return true for the test token
    when(turnstileService.verify(any())).thenReturn(true);

    when(emailService.sendEmail(anyString(), anyString(), anyString()))
        .thenReturn(new ResponseEntity<>("Mock email sent successfully", HttpStatus.OK));
  }

  @Test
  void completeAuthenticationFlow() throws Exception {
// Step 1: Register a new user
    RegisterRequest registerRequest = new RegisterRequest(
        "newuser@example.com",
        "Password123!",
        "New",
        "User",
        "turnstile-token"
    );

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.success").value(true));

    // Step 2: Manually set email as verified (simulating clicking the verification link)
    User user = userRepository.findByEmail("newuser@example.com").orElseThrow();
    user.setEmailVerified(true);
    userRepository.save(user);

    // Step 3: Login to get tokens
    LoginRequest loginRequest = new LoginRequest("newuser@example.com", "Password123!");
    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andReturn();

    LoginResponse loginResponse = objectMapper.readValue(
        loginResult.getResponse().getContentAsString(),
        LoginResponse.class
    );
    String accessToken = loginResponse.getAccessToken();
    String refreshToken = loginResponse.getRefreshToken();

    assertThat(accessToken).isNotNull().isNotEmpty();
    assertThat(refreshToken).isNotNull().isNotEmpty();

    // Step 4: Use the token to access a protected endpoint
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newuser@example.com"));

    // Step 5: Refresh the token
    RefreshRequest refreshRequest = new RefreshRequest(refreshToken);
    MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andReturn();

    LoginResponse refreshResponse = objectMapper.readValue(
        refreshResult.getResponse().getContentAsString(),
        LoginResponse.class
    );
    String newAccessToken = refreshResponse.getAccessToken();
    String newRefreshToken = refreshResponse.getRefreshToken();

    assertThat(newAccessToken).isNotNull().isNotEmpty();
    assertThat(newRefreshToken).isNotNull().isNotEmpty();

    // Step 6: Use the new token to access a protected endpoint
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + newAccessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("User"));

    // Step 7: Login with the same credentials
    LoginRequest loginRequest2 = new LoginRequest("newuser@example.com", "Password123!");
    MvcResult loginResult2 = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andReturn();

    LoginResponse loginResponse2 = objectMapper.readValue(
        loginResult2.getResponse().getContentAsString(),
        LoginResponse.class
    );
    String loginAccessToken = loginResponse2.getAccessToken();
    String loginRefreshToken = loginResponse2.getRefreshToken();

    assertThat(loginAccessToken).isNotNull().isNotEmpty();
    assertThat(loginRefreshToken).isNotNull().isNotEmpty();

    // Step 8: Use the login token to access a protected endpoint
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + loginAccessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("User"));
  }
}