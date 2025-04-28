package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.dto.JoinHouseholdRequest;
import stud.ntnu.krisefikser.household.service.HouseholdService;

import java.util.List;

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
     * Joins household by ID.
     *
     * @return DTO of the joined household.
     * @since 1.0
     */
    @Operation(summary = "Join household by ID", description = "Joins a household by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined household"),
            @ApiResponse(responseCode = "400", description = "Invalid household ID")
    })
    @PostMapping("/join")
    public ResponseEntity<HouseholdResponse> joinHousehold(
            @Parameter(description = "Household ID") @RequestBody JoinHouseholdRequest request
    ) {
        return ResponseEntity.ok(householdService.joinHousehold(request.getHouseholdId()));
    }

    public ResponseEntity<HouseholdResponse> setActiveHousehold(
            @Parameter(description = "Household ID") @RequestBody JoinHouseholdRequest request
    ) {
        return ResponseEntity.ok(householdService.setActiveHousehold(request.getHouseholdId()));
    }
}
