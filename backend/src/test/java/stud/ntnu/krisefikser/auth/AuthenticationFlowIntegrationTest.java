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
@ActiveProfiles("test") // Ensure test profile is active
public class AuthenticationFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void completeAuthenticationFlow() throws Exception {
    // Step 1: Register a new user
    RegisterRequest registerRequest = new RegisterRequest(
        "test-user-" + System.currentTimeMillis() + "@example.com", // Use unique email
        "password123",
        "New",
        "User"
    );

    // First try the registration and capture the result without assertions
    MvcResult registerAttempt = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andReturn();

    // Print error message if status is not 200
    if (registerAttempt.getResponse().getStatus() != 200) {
      System.err.println("Registration failed with status: " +
          registerAttempt.getResponse().getStatus());
      System.err.println("Response body: " +
          registerAttempt.getResponse().getContentAsString());
      // Print request that caused failure
      System.err.println("Request body: " +
          objectMapper.writeValueAsString(registerRequest));
    }

    // Now proceed with the actual test assertion
    MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andReturn();

    // Continue with rest of the test...
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
        .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
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
        .andExpect(jsonPath("$.email").value(registerRequest.getEmail()));

    // Step 5: Login with the same credentials
    LoginRequest loginRequest = new LoginRequest(registerRequest.getEmail(), "password123");
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }
}