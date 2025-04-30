package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.dto.JoinHouseholdRequest;
import stud.ntnu.krisefikser.household.service.HouseholdService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing households in the system.
 * Provides endpoints for household management operations.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
@Tag(name = "Household", description = "Household management APIs")
public class HouseholdController {
    /**
     * The household service for handling household-related operations.
     */
    private final HouseholdService householdService;

    /**
     * Retrieves all households from the system.
     *
     * @return ResponseEntity containing a list of all households.
     * @since 1.0
     */
    @Operation(summary = "Get all households of user", description = "Retrieves a list of all households that the user is a member of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved households")
    })
    @GetMapping("/all")
    public ResponseEntity<List<HouseholdResponse>> getAllHouseholds() {
        return ResponseEntity.ok(householdService.getUserHouseholds());
    }

    /**
     * Joins household by ID and sets it as active.
     *
     * @return DTO of the joined household.
     * @since 1.0
     */
    @Operation(summary = "Join household by ID", description = "Joins a household by its ID and sets it as active.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined household"),
            @ApiResponse(responseCode = "400", description = "Invalid household ID")
    })
    @PostMapping("/join")
    public ResponseEntity<HouseholdResponse> joinHousehold(
            @Parameter(description = "Household ID") @RequestBody JoinHouseholdRequest request) {
        return ResponseEntity.ok(householdService.joinHousehold(request.getHouseholdId()));
    }

    /**
     * Sets the active household for the current user.
     *
     * @param request Contains the household ID to set as active
     * @return ResponseEntity containing the newly set active household
     * @since 1.0
     */
    @Operation(summary = "Set active household", description = "Sets the specified household as the active household for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully set active household"),
            @ApiResponse(responseCode = "400", description = "Invalid household ID"),
            @ApiResponse(responseCode = "404", description = "Household not found"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the specified household")
    })
    @PostMapping("/active")
    public ResponseEntity<HouseholdResponse> setActiveHousehold(
            @Parameter(description = "Request containing the household ID to set as active", required = true) @RequestBody JoinHouseholdRequest request) {
        return ResponseEntity.ok(householdService.setActiveHousehold(request.getHouseholdId()));
    }

    /**
     * Retrieves the currently active household for the user.
     *
     * @return ResponseEntity containing the active household information
     * @since 1.0
     */
    @Operation(summary = "Get active household", description = "Retrieves the currently active household for the logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active household"),
            @ApiResponse(responseCode = "404", description = "No active household set")
    })
    @GetMapping("/active")
    public ResponseEntity<HouseholdResponse> getActiveHousehold() {
        return ResponseEntity.ok(householdService.toHouseholdResponse(householdService.getActiveHousehold()));
    }

    /**
     * Leaves the specified household.
     *
     * @param request Contains the household ID to leave
     * @since 1.0
     */
    @Operation(summary = "Leave household", description = "Leaves the specified household")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left household"),
            @ApiResponse(responseCode = "400", description = "Invalid household ID"),
            @ApiResponse(responseCode = "404", description = "Household not found"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the specified household")
    })
    @PostMapping("/leave")
    public void leaveHousehold(
            @Parameter(description = "Household ID") @RequestBody JoinHouseholdRequest request) {
        householdService.leaveHousehold(request.getHouseholdId());
    }

    /**
     * Deletes the specified household.
     *
     * @param id The ID of the household to delete
     * @since 1.0
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete household", description = "Deletes the specified household")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted household"),
            @ApiResponse(responseCode = "400", description = "Invalid household ID"),
            @ApiResponse(responseCode = "404", description = "Household not found")
    })
    public void deleteHousehold(
            @Parameter(description = "Household ID") @PathVariable UUID id) {
        householdService.deleteHousehold(id);
    }

    /**
     * Creates a new household and sets it as active.
     *
     * @return ResponseEntity containing the created household
     * @since 1.0
     */
    @Operation(summary = "Create household", description = "Creates a new household and sets it as active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created household"),
            @ApiResponse(responseCode = "400", description = "Invalid household data")
    })
    @PostMapping
    public ResponseEntity<HouseholdResponse> createHousehold(
            @Parameter(description = "Household data") @RequestBody CreateHouseholdRequest household) {
        return ResponseEntity.status(201).body(householdService.createHousehold(household));
    }
}