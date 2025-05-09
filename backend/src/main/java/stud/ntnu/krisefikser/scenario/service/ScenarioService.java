package stud.ntnu.krisefikser.scenario.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.scenario.dto.CreateScenarioRequest;
import stud.ntnu.krisefikser.scenario.dto.ScenarioResponse;
import stud.ntnu.krisefikser.scenario.entity.Scenario;
import stud.ntnu.krisefikser.scenario.repository.ScenarioRepository;

/**
 * Service responsible for managing scenarios in the emergency preparedness system.
 *
 * <p>
 * This service provides functionality for creating, retrieving, updating, and deleting scenarios.
 * It interacts with the {@link ScenarioRepository} for data access operations.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ScenarioService {

  /**
   * Repository for Scenario entity operations.
   */
  private final ScenarioRepository scenarioRepository;

  /**
   * Creates a new scenario in the system.
   *
   * @param request the DTO containing scenario details
   * @return a response DTO containing the created scenario
   */
  @Transactional
  public ScenarioResponse createScenario(CreateScenarioRequest request) {
    Scenario scenario = Scenario.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .build();

    return scenarioRepository.save(scenario).toResponse();
  }

  /**
   * Updates an existing scenario.
   *
   * @param id      the unique identifier of the scenario to update
   * @param request the DTO containing updated scenario details
   * @return a response DTO containing the updated scenario
   * @throws EntityNotFoundException if no scenario exists with the given ID
   */
  @Transactional
  public ScenarioResponse updateScenario(UUID id, CreateScenarioRequest request) {
    return scenarioRepository.findById(id)
        .map(scenario -> {
          scenario.setTitle(request.getTitle());
          scenario.setContent(request.getContent());
          return scenarioRepository.save(scenario).toResponse();
        })
        .orElseThrow(() -> new EntityNotFoundException("Scenario not found with id: " + id));
  }

  /**
   * Deletes a scenario from the system.
   *
   * @param id the unique identifier of the scenario to delete
   * @throws EntityNotFoundException if no scenario exists with the given ID
   */
  @Transactional
  public void deleteScenario(UUID id) {
    if (!scenarioRepository.existsById(id)) {
      throw new EntityNotFoundException("Scenario not found with id: " + id);
    }
    scenarioRepository.deleteById(id);
  }

  /**
   * Retrieves a specific scenario by its ID.
   *
   * @param id the unique identifier of the scenario to retrieve
   * @return a response DTO containing the scenario details
   * @throws EntityNotFoundException if no scenario exists with the given ID
   */
  public ScenarioResponse getScenarioById(UUID id) {
    return scenarioRepository.findById(id)
        .map(Scenario::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Scenario not found with id: " + id));
  }

  /**
   * Retrieves all scenarios in the system.
   *
   * @return a list of scenario response DTOs
   */
  public List<ScenarioResponse> getAllScenarios() {
    return scenarioRepository.findAll().stream()
        .map(Scenario::toResponse)
        .toList();
  }
}