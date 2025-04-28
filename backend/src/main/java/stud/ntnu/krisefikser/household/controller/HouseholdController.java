package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.household.data.HouseholdResponse;
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
    @Operation(summary = "Get all households", description = "Retrieves a list of all households in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved households")
    })
    @GetMapping("/all")
    public ResponseEntity<List<HouseholdResponse>> getAllHouseholds() {
        return ResponseEntity.ok(householdService.getAllHouseholds());
    }
}
