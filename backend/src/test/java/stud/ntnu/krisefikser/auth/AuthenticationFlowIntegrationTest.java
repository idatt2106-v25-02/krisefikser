package stud.ntnu.krisefikser.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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

    MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andReturn();

    LoginResponse registerResponse = objectMapper.readValue(
        registerResult.getResponse().getContentAsString(),
        LoginResponse.class
    );
    String accessToken = registerResponse.getAccessToken();
    String refreshToken = registerResponse.getRefreshToken();

    assertThat(accessToken).isNotNull().isNotEmpty();
    assertThat(refreshToken).isNotNull().isNotEmpty();

    // Step 2: Use the token to access a protected endpoint
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("User"));

    // Step 3: Refresh the token
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

    // Step 4: Use the new token to access a protected endpoint
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + newAccessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newuser@example.com"));

    // Step 5: Login with the same credentials
    LoginRequest loginRequest = new LoginRequest("newuser@example.com", "Password123!");
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }
}