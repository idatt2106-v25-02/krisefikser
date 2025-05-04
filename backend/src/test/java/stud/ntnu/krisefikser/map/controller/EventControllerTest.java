package stud.ntnu.krisefikser.map.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class EventControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;
  
  @MockBean
  private EmailService emailService;
  
  @BeforeEach
  void setUp() {
    // Mock the EmailService to return successful response
    Mockito.when(emailService.sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(ResponseEntity.ok("Mock email sent successfully"));
  }

  private Event testEvent;

  @Test
  @WithMockUser(roles = "ADMIN")
  void createAndUpdateEvent() throws Exception {
    createTestEvent();
    testEvent.setTitle("Updated Title");
    mockMvc.perform(put("/api/events/" + testEvent.getId()).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testEvent)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").exists())
        .andReturn();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createAndDeleteEvent() throws Exception {
    createTestEvent();
    mockMvc.perform(delete("/api/events/" + testEvent.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  void getEvents() throws Exception {
    mockMvc.perform(get("/api/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  private void createTestEvent() throws Exception {
    testEvent =
        Event.builder()
            .title("Test Event")
            .description("Test Description")
            .level(EventLevel.YELLOW)
            .latitude(63.4305)
            .longitude(10.3951)
            .radius(1000.0)
            .startTime(LocalDateTime.now())
            .status(EventStatus.ONGOING)
            .endTime(null)
            .build();

    MvcResult mvcResult = mockMvc.perform(
            post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent))).andExpect(status().isOk())
        .andReturn();
    testEvent = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Event.class);
  }
}