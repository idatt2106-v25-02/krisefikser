package stud.ntnu.krisefikser.household.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import stud.ntnu.krisefikser.household.dto.MeetingPointRequest;
import stud.ntnu.krisefikser.household.dto.MeetingPointResponse;
import stud.ntnu.krisefikser.household.service.MeetingPointService;

@WebMvcTest(MeetingPointController.class)
@Import(TestSecurityConfig.class)
class MeetingPointControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MeetingPointService meetingPointService;
  @MockitoBean
  private TokenService tokenService;
  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @Test
  @WithMockUser
  void createMeetingPoint_shouldReturnCreatedResource() throws Exception {
    UUID householdId = UUID.randomUUID();
    MeetingPointRequest request = MeetingPointRequest.builder()
        .name("Skole")
        .description("Mote ved skolen")
        .latitude(63.4)
        .longitude(10.3)
        .build();
    MeetingPointResponse response = MeetingPointResponse.builder()
        .id(UUID.randomUUID())
        .name("Skole")
        .description("Mote ved skolen")
        .latitude(63.4)
        .longitude(10.3)
        .householdId(householdId)
        .build();

    when(meetingPointService.createMeetingPoint(any(UUID.class), any(MeetingPointRequest.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/households/{householdId}/meeting-points", householdId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Skole"));
  }

  @Test
  @WithMockUser
  void getMeetingPoints_shouldReturnList() throws Exception {
    UUID householdId = UUID.randomUUID();
    MeetingPointResponse response = MeetingPointResponse.builder()
        .id(UUID.randomUUID())
        .name("Skole")
        .description("Mote ved skolen")
        .latitude(63.4)
        .longitude(10.3)
        .householdId(householdId)
        .build();
    when(meetingPointService.getMeetingPointsByHousehold(householdId)).thenReturn(List.of(response));

    mockMvc.perform(get("/api/households/{householdId}/meeting-points", householdId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Skole"));
  }
}
