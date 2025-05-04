package stud.ntnu.krisefikser.scenario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
import stud.ntnu.krisefikser.scenario.dto.CreateScenarioRequest;
import stud.ntnu.krisefikser.scenario.dto.ScenarioResponse;
import stud.ntnu.krisefikser.scenario.service.ScenarioService;

@WebMvcTest(controllers = ScenarioController.class)
@Import(TestSecurityConfig.class)
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScenarioService scenarioService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private TokenService tokenService;

    private UUID validId;
    private CreateScenarioRequest createScenarioRequest;
    private ScenarioResponse scenarioResponse;

    @BeforeEach
    void setUp() {
        validId = UUID.randomUUID();

        createScenarioRequest = new CreateScenarioRequest(
                "Flood Emergency",
                "# Flood Emergency Guide\n\nThis is what to do in case of a flood.");

        scenarioResponse = new ScenarioResponse(
                validId,
                "Flood Emergency",
                "# Flood Emergency Guide\n\nThis is what to do in case of a flood.");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createScenario_AsAdmin_ShouldReturnCreatedScenario() throws Exception {
        // Arrange
        when(scenarioService.createScenario(any(CreateScenarioRequest.class)))
                .thenReturn(scenarioResponse);

        // Act & Assert
        mockMvc.perform(post("/api/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validId.toString()))
                .andExpect(jsonPath("$.title").value("Flood Emergency"))
                .andExpect(jsonPath("$.content")
                        .value("# Flood Emergency Guide\n\nThis is what to do in case of a flood."));
    }

    @Test
    @WithMockUser
    void createScenario_AsRegularUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createScenario_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateScenario_AsAdmin_ShouldReturnUpdatedScenario() throws Exception {
        // Arrange
        when(scenarioService.updateScenario(eq(validId), any(CreateScenarioRequest.class)))
                .thenReturn(scenarioResponse);

        // Act & Assert
        mockMvc.perform(put("/api/scenarios/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validId.toString()))
                .andExpect(jsonPath("$.title").value("Flood Emergency"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateScenario_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scenarioService.updateScenario(eq(nonExistentId), any(CreateScenarioRequest.class)))
                .thenThrow(new EntityNotFoundException("Scenario not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(put("/api/scenarios/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateScenario_AsRegularUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/scenarios/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateScenario_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/scenarios/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createScenarioRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteScenario_AsAdmin_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(scenarioService).deleteScenario(validId);

        // Act & Assert
        mockMvc.perform(delete("/api/scenarios/" + validId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteScenario_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Scenario not found with id: " + nonExistentId))
                .when(scenarioService).deleteScenario(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/scenarios/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteScenario_AsRegularUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/scenarios/" + validId))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteScenario_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/scenarios/" + validId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getScenarioById_ShouldReturnScenario() throws Exception {
        // Arrange
        when(scenarioService.getScenarioById(validId)).thenReturn(scenarioResponse);

        // Act & Assert
        mockMvc.perform(get("/api/scenarios/" + validId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validId.toString()))
                .andExpect(jsonPath("$.title").value("Flood Emergency"))
                .andExpect(jsonPath("$.content")
                        .value("# Flood Emergency Guide\n\nThis is what to do in case of a flood."));
    }

    @Test
    @WithMockUser
    void getScenarioById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scenarioService.getScenarioById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Scenario not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/scenarios/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getScenarioById_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/scenarios/" + validId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getAllScenarios_ShouldReturnScenariosList() throws Exception {
        // Arrange
        List<ScenarioResponse> scenarios = List.of(scenarioResponse);
        when(scenarioService.getAllScenarios()).thenReturn(scenarios);

        // Act & Assert
        mockMvc.perform(get("/api/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(validId.toString()))
                .andExpect(jsonPath("$[0].title").value("Flood Emergency"))
                .andExpect(jsonPath("$[0].content")
                        .value("# Flood Emergency Guide\n\nThis is what to do in case of a flood."));
    }

    @Test
    @WithMockUser
    void getAllScenarios_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(scenarioService.getAllScenarios()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllScenarios_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/scenarios"))
                .andExpect(status().isUnauthorized());
    }
}