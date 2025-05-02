package stud.ntnu.krisefikser.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

  private static final String TEST_USER_EMAIL = "brotherman@testern.no";
  private static final String DEFAULT_PASSWORD = "password";
  private static final String TEST_HOUSEHOLD_NAME = "Test Household";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  private String accessToken;

  @Getter
  private User testUser;

  @Autowired
  private DatabaseCleanupService databaseCleanupService;

  public MockHttpServletRequestBuilder withJwtAuth(MockHttpServletRequestBuilder requestBuilder) {
    return requestBuilder.header("Authorization", "Bearer " + accessToken);
  }

  public void setUpUser() throws Exception {
    // Delete brotherman testern if exists
    databaseCleanupService.clearDatabase();

    // Login if user exists
    this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
        .orElse(null);
    if (this.testUser != null) {
      LoginRequest request = new LoginRequest(
          TEST_USER_EMAIL,
          DEFAULT_PASSWORD
      );

      Map<String, String> responseMap = getPostResponse(
          "/api/auth/login",
          objectMapper.writeValueAsString(request)
      );

      this.accessToken = responseMap.get("accessToken");

      if (this.testUser.getActiveHousehold() == null) {
        createHousehold();
      }

      return;
    }

    // Create User
    RegisterRequest request = new RegisterRequest(
        TEST_USER_EMAIL,
        DEFAULT_PASSWORD,
        "Test",
        "User",
        "turnstile-token"
    );

    Map<String, String> responseMap = getPostResponse(
        "/api/auth/register",
        objectMapper.writeValueAsString(request)
    );

    this.accessToken = responseMap.get("accessToken");

    // Fetch the user from the database
    this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
        .orElseThrow(() -> new RuntimeException("Test user not found"));

    createHousehold();

    // Re-fetch the user with the active household
    this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
        .orElseThrow(() -> new RuntimeException("Test user not found"));

    log.info("Access token: {}", accessToken);
    log.info("Test user: {}", testUser);
    log.info("Test household: {}", getTestHousehold());
  }

  public Household getTestHousehold() {
    return getTestUser().getActiveHousehold();
  }

  private Map<String, String> getPostResponse(String uri, String body) {
    try {
      String responseContent = mockMvc.perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                      uri)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(
              org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
          .andReturn()
          .getResponse()
          .getContentAsString();

      // Parse response and extract token
      return objectMapper.readValue(responseContent,
          new TypeReference<>() {
          });
    } catch (Exception e) {
      log.error("Failed to parse response", e);
      throw new RuntimeException("Failed to parse response", e);
    }
  }

  private void createHousehold() {
    CreateHouseholdRequest createHouseholdRequest = CreateHouseholdRequest.builder()
        .name(TEST_HOUSEHOLD_NAME)
        .latitude(0.0)
        .longitude(0.0)
        .address("Test Address")
        .city("Test City")
        .postalCode("12345")
        .build();

    try {
      mockMvc.perform(
              withJwtAuth(
                  org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                          "/api/households")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(createHouseholdRequest))
              )
          )
          .andExpect(
              org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                  .isCreated())
          .andReturn();
    } catch (Exception e) {
      log.error("Failed to create household", e);
      throw new RuntimeException("Failed to create household", e);
    }

  }
}
