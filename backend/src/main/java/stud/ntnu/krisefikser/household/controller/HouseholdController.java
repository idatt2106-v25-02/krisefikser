package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import stud.ntnu.krisefikser.household.dto.CreateGuestRequest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.dto.JoinHouseholdRequest;
import stud.ntnu.krisefikser.household.service.HouseholdService;

/**
 * REST controller for managing households in the system. Provides endpoints for household
 * management operations.
 */
@Slf4j
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
  @Operation(summary = "Get all households of user",
      description = "Retrieves a list of all households that the user is a member of")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved households")
  })
  @GetMapping("/all")
  public ResponseEntity<List<HouseholdResponse>> getAllUserHouseholds() {
    return ResponseEntity.ok(householdService.getUserHouseholds());
  }

  /**
   * Joins household by ID and sets it as active.
   *
   * @return DTO of the joined household.
   * @since 1.0
   */
  @Operation(summary = "Join household by ID",
      description = "Joins a household by its ID and sets it as active.")
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
  @Operation(summary = "Set active household",
      description = "Sets the specified household as the active household for the current user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully set active household"),
      @ApiResponse(responseCode = "400", description = "Invalid household ID"),
      @ApiResponse(responseCode = "404", description = "Household not found"),
      @ApiResponse(responseCode = "403",
          description = "User is not a member of the specified household")
  })
  @PostMapping("/active")
  public ResponseEntity<HouseholdResponse> setActiveHousehold(
      @Parameter(description = "Request containing the household ID to set as active",
          required = true) @RequestBody JoinHouseholdRequest request) {
    return ResponseEntity.ok(householdService.setActiveHousehold(request.getHouseholdId()));
  }

  /**
   * Retrieves the currently active household for the user.
   *
   * @return ResponseEntity containing the active household information
   * @since 1.0
   */
  @Operation(summary = "Get active household",
      description = "Retrieves the currently active household for the logged-in user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved active household"),
      @ApiResponse(responseCode = "404", description = "No active household set")
  })
  @GetMapping("/active")
  public ResponseEntity<HouseholdResponse> getActiveHousehold() {
    return ResponseEntity.ok(
        householdService.toHouseholdResponse(householdService.getActiveHousehold()));
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
      @ApiResponse(responseCode = "403",
          description = "User is not a member of the specified household")
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
  @Operation(summary = "Create household",
      description = "Creates a new household and sets it as active")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created household"),
      @ApiResponse(responseCode = "400", description = "Invalid household data")
  })
  @PostMapping
  public ResponseEntity<HouseholdResponse> createHousehold(
      @Parameter(description = "Household data") @RequestBody CreateHouseholdRequest household) {
    return ResponseEntity.status(201).body(householdService.createHousehold(household));
  }

  /**
   * Update active household information.
   *
   * @param createRequest The updated household data (create household request)
   * @return ResponseEntity containing the updated household
   */
  @Operation(summary = "Update active household",
      description = "Updates the active household with the provided data")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated active household"),
      @ApiResponse(responseCode = "400", description = "Invalid household data"),
      @ApiResponse(responseCode = "404", description = "Household not found")
  })
  @PutMapping("/active")
  public ResponseEntity<HouseholdResponse> updateActiveHousehold(
      @Parameter(description = "Household data") @RequestBody
      CreateHouseholdRequest createRequest) {
    return ResponseEntity.ok(householdService.updateActiveHousehold(createRequest));
  }

  /**
   * Owner can remove member from household.
   *
   * @param householdId The ID of the household
   * @param userId      The ID of the user to remove
   * @return ResponseEntity containing the updated household
   * @since 1.0
   */
  @Operation(summary = "Remove member from household (Owner only)",
      description = "Removes a user from the household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed member"),
      @ApiResponse(responseCode = "400", description = "Invalid household or user ID"),
      @ApiResponse(responseCode = "404", description = "Household or user not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - owner role required")
  })
  @DeleteMapping("/{householdId}/members/{userId}")
  public ResponseEntity<HouseholdResponse> removeMemberFromHouseholdOwner(
      @Parameter(description = "Household ID") @PathVariable UUID householdId,
      @Parameter(description = "User ID") @PathVariable UUID userId
  ) {
    return ResponseEntity.ok(householdService.removeMemberFromHousehold(householdId, userId));
  }

  /**
   * Add a guest to the household.
   */
  @Operation(summary = "Add guest to current household",
      description = "Adds a guest to current household. A guest is a user who does not have a user "
          + "account.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully added guest"),
      @ApiResponse(responseCode = "400", description = "Invalid household or guest data"),
      @ApiResponse(responseCode = "404", description = "Household not found")
  })
  @PostMapping("/guests")
  public ResponseEntity<HouseholdResponse> addGuestToHousehold(
      @Parameter(description = "Guest data") @RequestBody CreateGuestRequest guest) {
    return ResponseEntity.ok(householdService.addGuestToHousehold(guest));
  }

  /**
   * Removes a guest from a household.
   *
   * @param guestId The ID of the guest to remove
   * @return ResponseEntity containing the updated household
   */
  @Operation(summary = "Remove guest from household",
      description = "Removes a guest from your household. You must be owner.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed guest"),
      @ApiResponse(responseCode = "400", description = "Invalid household or guest ID"),
      @ApiResponse(responseCode = "404", description = "Household or guest not found")
  })
  @DeleteMapping("/guests/{guestId}")
  public ResponseEntity<HouseholdResponse> removeGuestFromHousehold(
      @Parameter(description = "Guest ID") @PathVariable UUID guestId) {
    return ResponseEntity.ok(householdService.removeGuestFromHousehold(guestId));
  }

  /**
   * Retrieves all households in the system. Only accessible to users with ADMIN * role. * * @return
   * ResponseEntity containing a list of all households with their members
   *
   * @since 1.0
   */
  @Operation(summary = "Get all households (Admin only)",
      description = "Retrieves a list of all households in the system with their members")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved all households"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
  })
  @GetMapping("/admin/all")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<HouseholdResponse>> getAllHouseholdsAdmin() {
    return ResponseEntity.ok(householdService.getAllHouseholds());
  }

  /**
   * Adds a member to a household. Only accessible to users with ADMIN role. * * @param householdId
   * The ID of the household
   *
   * @param userId The ID of the user to add
   * @return ResponseEntity containing the updated household
   * @since 1.0
   */
  @Operation(summary = "Add member to household (Admin only)",
      description = "Adds a user as a member to a household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully added member"),
      @ApiResponse(responseCode = "400", description = "Invalid household or user ID"),
      @ApiResponse(responseCode = "404", description = "Household or user not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
  })
  @PostMapping("/admin/{householdId}/members/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<HouseholdResponse> addMemberToHousehold(
      @Parameter(description = "Household ID") @PathVariable UUID householdId,
      @Parameter(description = "User ID") @PathVariable UUID userId) {
    return ResponseEntity.ok(householdService.addMemberToHousehold(householdId, userId));
  }

  /**
   * Removes a member from a household. Only accessible to users with ADMIN role. * * @param
   * householdId The ID of the household
   *
   * @param userId The ID of the user to remove
   * @return ResponseEntity containing the updated household
   * @since 1.0
   */
  @Operation(summary = "Remove member from household (Admin only)",
      description = "Removes a user from a household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed member"),
      @ApiResponse(responseCode = "400", description = "Invalid household or user ID"),
      @ApiResponse(responseCode = "404", description = "Household or user not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
  })
  @DeleteMapping("/admin/{householdId}/members/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<HouseholdResponse> removeMemberFromHousehold(
      @Parameter(description = "Household ID") @PathVariable UUID householdId,
      @Parameter(description = "User ID") @PathVariable UUID userId) {
    return ResponseEntity.ok(householdService.removeMemberFromHousehold(householdId, userId));
  }

  /**
   * Updates a household. Only accessible to users with ADMIN role. * * @param id      The ID of the
   * household to update
   *
   * @param request The updated household data
   * @return ResponseEntity containing the updated household
   * @since 1.0
   */
  @Operation(summary = "Update household (Admin only)",
      description = "Updates a household's information")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated household"),
      @ApiResponse(responseCode = "400", description = "Invalid household data"),
      @ApiResponse(responseCode = "404", description = "Household not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
  })
  @PostMapping("/admin/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<HouseholdResponse> updateHousehold(
      @Parameter(description = "Household ID") @PathVariable UUID id,
      @Parameter(description = "Updated household data") @RequestBody
      CreateHouseholdRequest request) {
    return ResponseEntity.ok(householdService.updateHousehold(id, request));
  }

  /**
   * Deletes a household. Only accessible to users with ADMIN role. * This will cascade delete
   * household members but not users. * * @param id The ID of the household to delete
   *
   * @since 1.0
   */
  @Operation(summary = "Delete household (Admin only)",
      description = "Deletes a household and its members")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted household"),
      @ApiResponse(responseCode = "400", description = "Invalid household ID"),
      @ApiResponse(responseCode = "404", description = "Household not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
  })
  @DeleteMapping("/admin/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public void deleteHouseholdAdmin(
      @Parameter(description = "Household ID") @PathVariable UUID id) {
    householdService.deleteHouseholdAdmin(id);
  }
}