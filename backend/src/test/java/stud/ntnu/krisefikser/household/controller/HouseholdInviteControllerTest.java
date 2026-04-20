package stud.ntnu.krisefikser.household.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdInviteResponse;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite;
import stud.ntnu.krisefikser.household.service.HouseholdInviteService;

@WebMvcTest(HouseholdInviteController.class)
@Import(TestSecurityConfig.class)
class HouseholdInviteControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private HouseholdInviteService inviteService;
  @MockitoBean
  private TokenService tokenService;
  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @Test
  @WithMockUser
  void createInvite_shouldReturnCreatedInvite() throws Exception {
    UUID householdId = UUID.randomUUID();
    UUID inviteId = UUID.randomUUID();
    CreateHouseholdInviteRequest request = new CreateHouseholdInviteRequest(householdId, null,
        "invite@example.com");
    HouseholdInviteResponse response = new HouseholdInviteResponse(inviteId, null, null,
        "invite@example.com", null, LocalDateTime.now(), HouseholdInvite.InviteStatus.PENDING, null);

    when(inviteService.createInvite(any(CreateHouseholdInviteRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/household-invites")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(inviteId.toString()))
        .andExpect(jsonPath("$.invitedEmail").value("invite@example.com"));
  }

  @Test
  @WithMockUser
  void getPendingInvitesForUser_shouldReturnList() throws Exception {
    HouseholdInviteResponse response = new HouseholdInviteResponse(UUID.randomUUID(), null, null,
        "invite@example.com", null, LocalDateTime.now(), HouseholdInvite.InviteStatus.PENDING, null);
    when(inviteService.getPendingInvitesForUser()).thenReturn(List.of(response));

    mockMvc.perform(get("/api/household-invites/pending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].invitedEmail").value("invite@example.com"));
  }
}
