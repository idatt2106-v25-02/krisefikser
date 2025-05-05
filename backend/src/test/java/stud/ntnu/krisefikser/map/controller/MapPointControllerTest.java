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
import stud.ntnu.krisefikser.map.dto.MapPointRequest;
import stud.ntnu.krisefikser.map.dto.MapPointResponse;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.dto.UpdateMapPointRequest;
import stud.ntnu.krisefikser.map.service.MapPointService;
import stud.ntnu.krisefikser.map.service.MapPointTypeService;

@WebMvcTest(MapPointController.class)
@Import(TestSecurityConfig.class)
class MapPointControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MapPointService mapPointService;

  @MockitoBean
  private MapPointTypeService mapPointTypeService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private MapPointTypeResponse testMapPointTypeResponse;
  private MapPointResponse testMapPointResponse;
  private MapPointRequest testMapPointRequest;
  private UpdateMapPointRequest testUpdateMapPointRequest;
  private List<MapPointResponse> testMapPointResponses;

  @BeforeEach
  void setUp() {
    testMapPointTypeResponse = MapPointTypeResponse.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testMapPointResponse = MapPointResponse.builder()
        .id(1L)
        .latitude(63.4305)
        .longitude(10.3951)
        .type(testMapPointTypeResponse)
        .build();

    testMapPointRequest = MapPointRequest.builder()
        .latitude(63.4305)
        .longitude(10.3951)
        .typeId(1L)
        .build();

    testUpdateMapPointRequest = UpdateMapPointRequest.builder()
        .latitude(63.4305)
        .longitude(10.3951)
        .typeId(1L)
        .build();

    testMapPointResponses = List.of(testMapPointResponse);
  }

  @Test
  void getAllMapPoints_ShouldReturnList() throws Exception {
    when(mapPointService.getAllMapPoints()).thenReturn(testMapPointResponses);

    mockMvc.perform(get("/api/map-points"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(testMapPointResponse.getId()))
        .andExpect(jsonPath("$[0].latitude").value(testMapPointResponse.getLatitude()))
        .andExpect(jsonPath("$[0].longitude").value(testMapPointResponse.getLongitude()));
  }

  @Test
  void getMapPointById_ShouldReturnMapPoint() throws Exception {
    when(mapPointService.getMapPointById(1L)).thenReturn(testMapPointResponse);

    mockMvc.perform(get("/api/map-points/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPointResponse.getId()))
        .andExpect(jsonPath("$.latitude").value(testMapPointResponse.getLatitude()))
        .andExpect(jsonPath("$.longitude").value(testMapPointResponse.getLongitude()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createMapPoint_WithAdminRole_ShouldCreateAndReturnMapPoint() throws Exception {
    when(mapPointService.createMapPoint(any(MapPointRequest.class))).thenReturn(
        testMapPointResponse);

    mockMvc.perform(post("/api/map-points")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPointRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPointResponse.getId()))
        .andExpect(jsonPath("$.latitude").value(testMapPointResponse.getLatitude()))
        .andExpect(jsonPath("$.longitude").value(testMapPointResponse.getLongitude()));
  }

  @Test
  @WithMockUser(roles = "USER")
  void createMapPoint_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/map-points")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPointRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateMapPoint_WithAdminRole_ShouldUpdateAndReturnMapPoint() throws Exception {
    when(mapPointService.updateMapPoint(eq(1L), any(UpdateMapPointRequest.class))).thenReturn(
        testMapPointResponse);

    mockMvc.perform(put("/api/map-points/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateMapPointRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPointResponse.getId()))
        .andExpect(jsonPath("$.latitude").value(testMapPointResponse.getLatitude()))
        .andExpect(jsonPath("$.longitude").value(testMapPointResponse.getLongitude()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteMapPoint_WithAdminRole_ShouldReturnNoContent() throws Exception {
    doNothing().when(mapPointService).deleteMapPoint(1L);

    mockMvc.perform(delete("/api/map-points/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteMapPoint_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(delete("/api/map-points/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden());
  }
}