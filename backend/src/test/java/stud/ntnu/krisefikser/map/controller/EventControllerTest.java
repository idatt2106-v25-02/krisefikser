package stud.ntnu.krisefikser.map.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.map.dto.EventRequest;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.map.dto.UpdateEventRequest;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;
import stud.ntnu.krisefikser.map.service.EventService;

@WebMvcTest(EventController.class)
@Import(TestSecurityConfig.class)
class EventControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private EventService eventService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private EventResponse testEventResponse;
  private EventRequest testEventRequest;
  private UpdateEventRequest testUpdateEventRequest;
  private List<EventResponse> testEventResponses;
  private ZonedDateTime now;

  @BeforeEach
  void setUp() {
    now = ZonedDateTime.now();

    testEventResponse = EventResponse.builder()
        .id(1L)
        .title("Test Event")
        .description("Test Description")
        .radius(1000.0)
        .latitude(63.4305)
        .longitude(10.3951)
        .level(EventLevel.YELLOW)
        .startTime(now)
        .endTime(now.plusHours(2))
        .status(EventStatus.ONGOING)
        .build();

    testEventRequest = EventRequest.builder()
        .title("Test Event")
        .description("Test Description")
        .radius(1000.0)
        .latitude(63.4305)
        .longitude(10.3951)
        .level(EventLevel.YELLOW)
        .startTime(now)
        .endTime(now.plusHours(2))
        .status(EventStatus.ONGOING)
        .build();

    testUpdateEventRequest = UpdateEventRequest.builder()
        .title("Updated Event")
        .description("Updated Description")
        .radius(1500.0)
        .latitude(63.4305)
        .longitude(10.3951)
        .level(EventLevel.RED)
        .startTime(now)
        .endTime(now.plusHours(3))
        .status(EventStatus.ONGOING)
        .build();

    testEventResponses = List.of(testEventResponse);
  }

  @Test
  void getAllEvents_ShouldReturnList() throws Exception {
    when(eventService.getAllEvents()).thenReturn(testEventResponses);

    mockMvc.perform(get("/api/events"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(testEventResponse.getId()))
        .andExpect(jsonPath("$[0].title").value(testEventResponse.getTitle()))
        .andExpect(jsonPath("$[0].description").value(testEventResponse.getDescription()));
  }

  @Test
  void getEventById_ShouldReturnEvent() throws Exception {
    when(eventService.getEventById(1L)).thenReturn(testEventResponse);

    mockMvc.perform(get("/api/events/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testEventResponse.getId()))
        .andExpect(jsonPath("$.title").value(testEventResponse.getTitle()))
        .andExpect(jsonPath("$.description").value(testEventResponse.getDescription()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createEvent_WithAdminRole_ShouldCreateAndReturnEvent() throws Exception {
    when(eventService.createEvent(any(EventRequest.class))).thenReturn(testEventResponse);

    mockMvc.perform(post("/api/events")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testEventRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testEventResponse.getId()))
        .andExpect(jsonPath("$.title").value(testEventResponse.getTitle()))
        .andExpect(jsonPath("$.description").value(testEventResponse.getDescription()));
  }

  @Test
  @WithMockUser(roles = "USER")
  void createEvent_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/events")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testEventRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateEvent_WithAdminRole_ShouldUpdateAndReturnEvent() throws Exception {
    when(eventService.updateEvent(eq(1L), any(UpdateEventRequest.class))).thenReturn(
        testEventResponse);

    mockMvc.perform(put("/api/events/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateEventRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testEventResponse.getId()))
        .andExpect(jsonPath("$.title").value(testEventResponse.getTitle()))
        .andExpect(jsonPath("$.description").value(testEventResponse.getDescription()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteEvent_WithAdminRole_ShouldReturnNoContent() throws Exception {
    doNothing().when(eventService).deleteEvent(1L);

    mockMvc.perform(delete("/api/events/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteEvent_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(delete("/api/events/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden());
  }
}