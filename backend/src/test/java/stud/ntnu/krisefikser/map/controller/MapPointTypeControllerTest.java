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
import stud.ntnu.krisefikser.map.dto.MapPointTypeRequest;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.dto.UpdateMapPointTypeRequest;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.service.MapPointTypeService;

@WebMvcTest(MapPointTypeController.class)
@Import(TestSecurityConfig.class)
class MapPointTypeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MapPointTypeService mapPointTypeService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private MapPointTypeResponse testMapPointTypeResponse;
  private MapPointTypeRequest testMapPointTypeRequest;
  private UpdateMapPointTypeRequest testUpdateMapPointTypeRequest;
  private List<MapPointTypeResponse> testMapPointTypeResponses;

  @BeforeEach
  void setUp() {
    testMapPointTypeResponse = MapPointTypeResponse.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testMapPointTypeRequest = MapPointTypeRequest.builder()
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testUpdateMapPointTypeRequest = UpdateMapPointTypeRequest.builder()
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testMapPointTypeResponses = List.of(testMapPointTypeResponse);
  }

  @Test
  void getAllMapPointTypes_ShouldReturnList() throws Exception {
    when(mapPointTypeService.getAllMapPointTypes()).thenReturn(testMapPointTypeResponses);

    mockMvc.perform(get("/api/map-point-types"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(testMapPointTypeResponse.getId()))
        .andExpect(jsonPath("$[0].title").value(testMapPointTypeResponse.getTitle()))
        .andExpect(jsonPath("$[0].iconUrl").value(testMapPointTypeResponse.getIconUrl()));
  }

  @Test
  void getMapPointTypeById_ShouldReturnMapPointType() throws Exception {
    // Create an entity to be returned by the service
    MapPointType mapPointType = MapPointType.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    // Mock the service to return the entity
    when(mapPointTypeService.getMapPointTypeById(1L)).thenReturn(mapPointType);

    mockMvc.perform(get("/api/map-point-types/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPointTypeResponse.getId()))
        .andExpect(jsonPath("$.title").value(testMapPointTypeResponse.getTitle()))
        .andExpect(jsonPath("$.iconUrl").value(testMapPointTypeResponse.getIconUrl()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createMapPointType_WithAdminRole_ShouldCreateAndReturnMapPointType() throws Exception {
    when(mapPointTypeService.createMapPointType(any(MapPointTypeRequest.class))).thenReturn(
        testMapPointTypeResponse);

    mockMvc.perform(post("/api/map-point-types")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPointTypeRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPointTypeResponse.getId()))
        .andExpect(jsonPath("$.title").value(testMapPointTypeResponse.getTitle()))
        .andExpect(jsonPath("$.iconUrl").value(testMapPointTypeResponse.getIconUrl()));
  }

  @Test
  @WithMockUser(roles = "USER")
  void createMapPointType_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post("/api/map-point-types")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testMapPointTypeRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateMapPointType_WithAdminRole_ShouldUpdateAndReturnMapPointType() throws Exception {
    when(mapPointTypeService.updateMapPointType(eq(1L), any(UpdateMapPointTypeRequest.class)))
        .thenReturn(testMapPointTypeResponse);

    mockMvc.perform(put("/api/map-point-types/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateMapPointTypeRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testMapPointTypeResponse.getId()))
        .andExpect(jsonPath("$.title").value(testMapPointTypeResponse.getTitle()))
        .andExpect(jsonPath("$.iconUrl").value(testMapPointTypeResponse.getIconUrl()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteMapPointType_WithAdminRole_ShouldReturnNoContent() throws Exception {
    doNothing().when(mapPointTypeService).deleteMapPointType(1L);

    mockMvc.perform(delete("/api/map-point-types/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteMapPointType_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(delete("/api/map-point-types/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden());
  }
}