package stud.ntnu.krisefikser.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.scenario.dto.CreateScenarioRequest;
import stud.ntnu.krisefikser.scenario.dto.ScenarioResponse;
import stud.ntnu.krisefikser.scenario.entity.Scenario;
import stud.ntnu.krisefikser.scenario.repository.ScenarioRepository;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

    @Mock
    private ScenarioRepository scenarioRepository;

    @InjectMocks
    private ScenarioService scenarioService;

    private UUID scenarioId;
    private Scenario scenario;
    private CreateScenarioRequest createRequest;

    @BeforeEach
    void setUp() {
        scenarioId = UUID.randomUUID();

        scenario = Scenario.builder()
                .id(scenarioId)
                .title("Flood Emergency")
                .content("# Flood Emergency Guide\n\nThis is what to do in case of a flood.")
                .build();

        createRequest = new CreateScenarioRequest(
                "Flood Emergency",
                "# Flood Emergency Guide\n\nThis is what to do in case of a flood.");
    }

    @Test
    void createScenario_ShouldReturnCreatedScenario() {
        // Arrange
        when(scenarioRepository.save(any(Scenario.class))).thenReturn(scenario);

        // Act
        ScenarioResponse result = scenarioService.createScenario(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(scenarioId);
        assertThat(result.getTitle()).isEqualTo("Flood Emergency");
        assertThat(result.getContent()).contains("Flood Emergency Guide");

        verify(scenarioRepository).save(any(Scenario.class));
    }

    @Test
    void updateScenario_WhenScenarioExists_ShouldReturnUpdatedScenario() {
        // Arrange
        Scenario updatedScenario = Scenario.builder()
                .id(scenarioId)
                .title("Updated Flood Emergency")
                .content("# Updated Flood Emergency Guide\n\nUpdated content.")
                .build();

        CreateScenarioRequest updateRequest = new CreateScenarioRequest(
                "Updated Flood Emergency",
                "# Updated Flood Emergency Guide\n\nUpdated content.");

        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenario));
        when(scenarioRepository.save(any(Scenario.class))).thenReturn(updatedScenario);

        // Act
        ScenarioResponse result = scenarioService.updateScenario(scenarioId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(scenarioId);
        assertThat(result.getTitle()).isEqualTo("Updated Flood Emergency");
        assertThat(result.getContent()).contains("Updated content");

        verify(scenarioRepository).findById(scenarioId);
        verify(scenarioRepository).save(any(Scenario.class));
    }

    @Test
    void updateScenario_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scenarioRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scenarioService.updateScenario(nonExistentId, createRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Scenario not found with id: " + nonExistentId);

        verify(scenarioRepository).findById(nonExistentId);
    }

    @Test
    void deleteScenario_WhenScenarioExists_ShouldDeleteScenario() {
        // Arrange
        when(scenarioRepository.existsById(scenarioId)).thenReturn(true);

        // Act
        scenarioService.deleteScenario(scenarioId);

        // Assert
        verify(scenarioRepository).existsById(scenarioId);
        verify(scenarioRepository).deleteById(scenarioId);
    }

    @Test
    void deleteScenario_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scenarioRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> scenarioService.deleteScenario(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Scenario not found with id: " + nonExistentId);

        verify(scenarioRepository).existsById(nonExistentId);
    }

    @Test
    void getScenarioById_WhenScenarioExists_ShouldReturnScenario() {
        // Arrange
        when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenario));

        // Act
        ScenarioResponse result = scenarioService.getScenarioById(scenarioId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(scenarioId);
        assertThat(result.getTitle()).isEqualTo("Flood Emergency");
        assertThat(result.getContent()).contains("Flood Emergency Guide");

        verify(scenarioRepository).findById(scenarioId);
    }

    @Test
    void getScenarioById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scenarioRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scenarioService.getScenarioById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Scenario not found with id: " + nonExistentId);

        verify(scenarioRepository).findById(nonExistentId);
    }

    @Test
    void getAllScenarios_ShouldReturnAllScenarios() {
        // Arrange
        List<Scenario> scenarios = List.of(scenario);
        when(scenarioRepository.findAll()).thenReturn(scenarios);

        // Act
        List<ScenarioResponse> result = scenarioService.getAllScenarios();

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(scenarioId);
        assertThat(result.getFirst().getTitle()).isEqualTo("Flood Emergency");
        assertThat(result.getFirst().getContent()).contains("Flood Emergency Guide");

        verify(scenarioRepository).findAll();
    }

    @Test
    void getAllScenarios_WhenNoScenarios_ShouldReturnEmptyList() {
        // Arrange
        when(scenarioRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ScenarioResponse> result = scenarioService.getAllScenarios();

        // Assert
        assertThat(result).isNotNull().isEmpty();

        verify(scenarioRepository).findAll();
    }
}