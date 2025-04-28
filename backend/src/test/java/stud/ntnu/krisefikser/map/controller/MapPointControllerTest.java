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
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.service.MapPointService;

@WebMvcTest(MapPointController.class)
@Import(TestSecurityConfig.class)
class MapPointControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MapPointService mapPointService;

  @Autowired
  private ObjectMapper objectMapper;

  private MapPoint testMapPoint;
  private MapPointType testMapPointType;
  private List<MapPoint> testMapPoints;

  @BeforeEach
  void setUp() {
    testMapPointType = MapPointType.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testMapPoint = MapPoint.builder()
        .id(1L)
        .latitude(63.4305)
        .longitude(10.3951)
        .type(testMapPointType)
        .build();

    testMapPoints = Arrays.asList(testMapPoint);
  }

  @Test
  void getAllMapPoints_ShouldReturnList() throws Exception {
    when(mapPointService.getAllMapPoints()).thenReturn(testMapPoints);

    mockMvc.perform(get("/api/map-points"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(testMapPoint.getId()))
        .andExpect(jsonPath("$[0].latitude").value(testMapPoint.getLatitude()))
        .andExpect(jsonPath("$[0].longitude").value(testMapPoint.getLongitude()));
  }

  @Test
  void getMapPointById_ShouldReturnMapPoint() throws Exception {
    when(mapPointService.getMapPointById(1L)).thenReturn(testMapPoint);

    mockMvc.perform(get("/api/map-points/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPoint.getId()))
        .andExpect(jsonPath("$.latitude").value(testMapPoint.getLatitude()))
        .andExpect(jsonPath("$.longitude").value(testMapPoint.getLongitude()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createMapPoint_WithAdminRole_ShouldCreateAndReturnMapPoint() throws Exception {
    when(mapPointService.createMapPoint(any(MapPoint.class))).thenReturn(testMapPoint);

    mockMvc.perform(post("/api/map-points")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPoint)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPoint.getId()))
        .andExpect(jsonPath("$.latitude").value(testMapPoint.getLatitude()))
        .andExpect(jsonPath("$.longitude").value(testMapPoint.getLongitude()));
  }

  @Test
  void createMapPoint_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/map-points")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPoint)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateMapPoint_WithAdminRole_ShouldUpdateAndReturnMapPoint() throws Exception {
    when(mapPointService.updateMapPoint(eq(1L), any(MapPoint.class))).thenReturn(testMapPoint);

    mockMvc.perform(put("/api/map-points/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPoint)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPoint.getId()))
        .andExpect(jsonPath("$.latitude").value(testMapPoint.getLatitude()))
        .andExpect(jsonPath("$.longitude").value(testMapPoint.getLongitude()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteMapPoint_WithAdminRole_ShouldReturnNoContent() throws Exception {
    doNothing().when(mapPointService).deleteMapPoint(1L);

    mockMvc.perform(delete("/api/map-points/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteMapPoint_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(delete("/api/map-points/1"))
        .andExpect(status().isForbidden());
  }
}