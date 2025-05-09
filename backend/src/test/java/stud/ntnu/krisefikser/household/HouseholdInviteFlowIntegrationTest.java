package stud.ntnu.krisefikser.household;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

class HouseholdInviteFlowIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  private User invitedUser;
  private String invitedUserToken;

  @BeforeEach
  void setUp() throws Exception {

    // Set up the test user and household
    setUpUser();

    // Create and register another user to be invited
    RegisterRequest registerRequest = new RegisterRequest(
        "invited@test.com",
        "Password1!",
        "Invited",
        "User",
        "turnstile-token");

    // Register the user
    mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk());

    // Retrieve user from repository
    this.invitedUser = userRepository.findByEmail("invited@test.com")
        .orElseThrow(() -> new RuntimeException("Invited user not found"));

    // Manually set email as verified
    this.invitedUser.setEmailVerified(true);
    userRepository.save(this.invitedUser);

    // Login to get the access token
    Map<String, String> loginRequest = Map.of(
        "email", "invited@test.com",
        "password", "Password1!"
    );

    String loginResponseContent = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Parse response and extract token
    Map<String, String> responseMap = objectMapper.readValue(loginResponseContent,
        new TypeReference<>() {
        });

    this.invitedUserToken = responseMap.get("accessToken");
  }

  @Test
  void completeInviteFlow() throws Exception {
    // 1. Create invite by user ID
    CreateHouseholdInviteRequest createRequest = new CreateHouseholdInviteRequest(
        getTestHousehold().getId(),
        invitedUser.getId(),
        null);

    MvcResult createResult = mockMvc.perform(
            withJwtAuth(
                post("/api/household-invites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper
                        .writeValueAsString(createRequest))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitedUser.id").value(invitedUser.getId().toString()))
        .andExpect(jsonPath("$.status").value(InviteStatus.PENDING.name()))
        .andReturn();

    String responseContent = createResult.getResponse().getContentAsString();
    UUID inviteId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

    // 2. Get pending invites for household
    mockMvc.perform(
            withJwtAuth(
                get("/api/household-invites/household/" + getTestHousehold().getId()
                    + "/pending")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(inviteId.toString()))
        .andExpect(jsonPath("$[0].status").value(InviteStatus.PENDING.name()));

    // 3. Switch to invited user context and get their pending invites
    mockMvc.perform(
            get("/api/household-invites/pending")
                .header("Authorization", "Bearer " + invitedUserToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(inviteId.toString()));

    // 4. Accept the invite as invited user
    mockMvc.perform(
            post("/api/household-invites/" + inviteId + "/accept")
                .header("Authorization", "Bearer " + invitedUserToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(InviteStatus.ACCEPTED.name()));

    // 6. Create invite by email
    createRequest = new CreateHouseholdInviteRequest(
        getTestHousehold().getId(),
        null,
        "newinvite@test.com");

    MvcResult emailInviteResult = mockMvc.perform(
            withJwtAuth(
                post("/api/household-invites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper
                        .writeValueAsString(createRequest))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitedEmail").value("newinvite@test.com"))
        .andExpect(jsonPath("$.status").value(InviteStatus.PENDING.name()))
        .andReturn();

    UUID emailInviteId = UUID.fromString(
        objectMapper.readTree(emailInviteResult.getResponse().getContentAsString())
            .get("id")
            .asText());

    // 7. Cancel the email invite
    mockMvc.perform(
            withJwtAuth(
                post("/api/household-invites/" + emailInviteId + "/cancel")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(InviteStatus.CANCELLED.name()));

    // 8. Create another invite for the same email - this should reuse the previous
    // invite
    MvcResult reusedInviteResult = mockMvc.perform(
            withJwtAuth(
                post("/api/household-invites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper
                        .writeValueAsString(createRequest))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitedEmail").value("newinvite@test.com"))
        .andExpect(jsonPath("$.status").value(InviteStatus.PENDING.name()))
        .andReturn();

    // Verify this is the same invite ID (reused)
    UUID reusedInviteId = UUID.fromString(
        objectMapper.readTree(reusedInviteResult.getResponse().getContentAsString())
            .get("id")
            .asText());

    // Assert that the invite was reused
    assert emailInviteId.equals(reusedInviteId) : "Expected the same invite to be reused";

    // 9. Register new user with the invited email
    RegisterRequest registerRequest = new RegisterRequest(
        "newinvite@test.com",
        "Password1!",
        "New",
        "User",
        "turnstile-token");

    mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk());

    // Find user and verify email
    User newUser = userRepository.findByEmail("newinvite@test.com")
        .orElseThrow(() -> new RuntimeException("New invited user not found"));
    newUser.setEmailVerified(true);
    userRepository.save(newUser);

    // Login to get the access token
    Map<String, String> loginRequest = Map.of(
        "email", "newinvite@test.com",
        "password", "Password1!"
    );

    String loginResponseContent = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Parse response and extract token
    String newUserToken = objectMapper.readValue(loginResponseContent,
            new TypeReference<Map<String, String>>() {
            })
        .get("accessToken");

    // 10. Decline the invite as the new user
    mockMvc.perform(
            post("/api/household-invites/" + reusedInviteId + "/decline")
                .header("Authorization", "Bearer " + newUserToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(InviteStatus.DECLINED.name()));
  }
}