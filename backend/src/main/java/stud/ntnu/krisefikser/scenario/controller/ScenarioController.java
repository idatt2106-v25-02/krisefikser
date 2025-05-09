package stud.ntnu.krisefikser.scenario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.scenario.dto.CreateScenarioRequest;
import stud.ntnu.krisefikser.scenario.dto.ScenarioResponse;
import stud.ntnu.krisefikser.scenario.service.ScenarioService;

/**
 * REST controller for managing emergency scenarios within the crisis management system.
 *
 * <p>This controller provides endpoints for creating, retrieving, updating, and deleting
 * scenarios.
 * Scenarios are informational resources containing guidance for different types of emergency
 * situations. Write operations (create, update, delete) are restricted to users with ADMIN role,
 * while read operations are accessible to all authenticated users.
 * </p>
 *
 * @author Krisefikser Development Team
 * @see ScenarioService
 * @see ScenarioResponse
 * @see CreateScenarioRequest
 * @since 1.0
 */
@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
@Tag(name = "Scenario", description = "Scenario management APIs")
public class ScenarioController {

  /**
   * Service component responsible for scenario operations. Handles creation, retrieval, update, and
   * deletion of scenarios.
   */
  private final ScenarioService scenarioService;

  /**
   * Creates a new scenario in the system.
   *
   * <p>This endpoint allows administrators to create scenarios with a title and content. Each
   * created scenario receives a unique identifier.
   * </p>
   *
   * @param createRequest the DTO containing the scenario details to be created. Must not be null
   *                      and should contain valid data.
   * @return ResponseEntity containing the created scenario's details with HTTP status 201 (Created)
   * @throws IllegalArgumentException if the request contains invalid data
   * @throws NullPointerException     if the request is null
   * @see CreateScenarioRequest
   * @see ScenarioResponse
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @Operation(summary = "Create a new scenario")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Scenario created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  public ResponseEntity<ScenarioResponse> createScenario(
      @Parameter(description = "Scenario data") @Valid @RequestBody CreateScenarioRequest createRequest) {
    ScenarioResponse createdScenario = scenarioService.createScenario(createRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdScenario);
  }

  /**
   * Updates an existing scenario in the system.
   *
   * <p>This endpoint allows administrators to update the title and content of an existing
   * scenario.
   * </p>
   *
   * @param id            the unique identifier of the scenario to update
   * @param updateRequest the DTO containing the updated scenario details
   * @return ResponseEntity containing the updated scenario's details with HTTP status 200 (OK)
   * @throws EntityNotFoundException  if no scenario with the specified ID exists
   * @throws IllegalArgumentException if the request contains invalid data
   * @see CreateScenarioRequest
   * @see ScenarioResponse
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @Operation(summary = "Update an existing scenario")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario updated successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  public ResponseEntity<ScenarioResponse> updateScenario(
      @Parameter(description = "ID of the scenario to update") @PathVariable("id") UUID id,
      @Parameter(description = "Updated scenario data") @Valid @RequestBody CreateScenarioRequest updateRequest) {
    ScenarioResponse updatedScenario = scenarioService.updateScenario(id, updateRequest);
    return ResponseEntity.ok(updatedScenario);
  }

  /**
   * Deletes a scenario from the system.
   *
   * <p>This endpoint allows administrators to permanently remove a scenario from the system.
   * </p>
   *
   * @param id the unique identifier of the scenario to delete
   * @return ResponseEntity with HTTP status 204 (No Content) indicating successful deletion
   * @throws EntityNotFoundException if no scenario with the specified ID exists
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @Operation(summary = "Delete a scenario")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions")
  })
  public ResponseEntity<Void> deleteScenario(
      @Parameter(description = "ID of the scenario to delete") @PathVariable("id") UUID id) {
    scenarioService.deleteScenario(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Retrieves a specific scenario by its ID.
   *
   * <p>This endpoint returns detailed information about a specific scenario, including its title
   * and content.
   * </p>
   *
   * @param id the unique identifier of the scenario to retrieve
   * @return ResponseEntity containing the scenario details with HTTP status 200 (OK)
   * @throws EntityNotFoundException if no scenario with the specified ID exists
   * @see ScenarioResponse
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get a specific scenario by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Scenario not found")
  })
  public ResponseEntity<ScenarioResponse> getScenarioById(
      @Parameter(description = "ID of the scenario to retrieve") @PathVariable("id") UUID id) {
    ScenarioResponse scenario = scenarioService.getScenarioById(id);
    return ResponseEntity.ok(scenario);
  }

  /**
   * Retrieves all scenarios in the system.
   *
   * <p>This endpoint returns a list of all scenarios, including their titles and content.
   * </p>
   *
   * @return ResponseEntity containing a list of all scenarios with HTTP status 200 (OK)
   * @see ScenarioResponse
   */
  @GetMapping
  @Operation(summary = "Get all scenarios")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenarios retrieved successfully")
  })
  public ResponseEntity<List<ScenarioResponse>> getAllScenarios() {
    List<ScenarioResponse> scenarios = scenarioService.getAllScenarios();
    return ResponseEntity.ok(scenarios);
  }
}