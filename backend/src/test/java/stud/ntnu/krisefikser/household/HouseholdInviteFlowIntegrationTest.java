package stud.ntnu.krisefikser.household;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite.InviteStatus;
import stud.ntnu.krisefikser.household.repository.HouseholdInviteRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

class HouseholdInviteFlowIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private HouseholdInviteRepository inviteRepository;

        @Autowired
        private HouseholdMemberRepository memberRepository;

        @Autowired
        private UserRepository userRepository;

        @MockBean
        private TurnstileService turnstileService;

        private User invitedUser;
        private String invitedUserToken;

        @BeforeEach
        void setUp() throws Exception {
                // Mock Turnstile verification
                when(turnstileService.verify(any())).thenReturn(true);

                // Set up the test user and household
                setUpUser();

                // Create and register another user to be invited
                RegisterRequest registerRequest = new RegisterRequest(
                                "invited@test.com",
                                "password",
                                "Invited",
                                "User",
                                "turnstile-token");

                String responseContent = mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // Parse response and extract token
                Map<String, String> responseMap = objectMapper.readValue(responseContent,
                                new TypeReference<>() {
                                });

                this.invitedUserToken = responseMap.get("accessToken");
                this.invitedUser = userRepository.findByEmail("invited@test.com")
                                .orElseThrow(() -> new RuntimeException("Invited user not found"));
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
                                "password",
                                "New",
                                "User",
                                "turnstile-token");

                String newUserResponse = mockMvc.perform(
                                post("/api/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                String newUserToken = objectMapper.readValue(newUserResponse, new TypeReference<Map<String, String>>() {
                })
                                .get("accessToken");

                // 10. Decline the invite as the new user - now should use the reused invite ID
                mockMvc.perform(
                                post("/api/household-invites/" + reusedInviteId + "/decline")
                                                .header("Authorization", "Bearer " + newUserToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(InviteStatus.DECLINED.name()));
        }
}