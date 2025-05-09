package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdInviteResponse;
import stud.ntnu.krisefikser.household.service.HouseholdInviteService;

/**
 * Controller for managing household invites.
 *
 * <p>This controller provides endpoints for creating, accepting, declining, and canceling
 * household invites. It also allows fetching pending invites for users and households.</p>
 */
@RestController
@RequestMapping("/api/household-invites")
@RequiredArgsConstructor
public class HouseholdInviteController {

  private final HouseholdInviteService inviteService;
  private final Logger log = LoggerFactory.getLogger(HouseholdInviteController.class);

  /**
   * Creates a new household invite.
   *
   * <p>This endpoint allows users to create a new invite for a household. The request must contain
   * the necessary details for the invite.</p>
   *
   * @param request the request object containing invite details
   * @return the created household invite response
   */
  @Operation(summary = "Create household invite",
      description = "Creates a new invitation to join a household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully created invite"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "404", description = "Household or user not found")
  })
  @PostMapping
  public ResponseEntity<HouseholdInviteResponse> createInvite(
      @Parameter(description = "Invite details") @Valid @RequestBody CreateHouseholdInviteRequest request) {
    return ResponseEntity.ok(inviteService.createInvite(request));
  }

  /**
   * Accepts a household invite.
   *
   * <p>This endpoint allows users to accept an existing household invite by its ID.</p>
   *
   * @param inviteId the ID of the invite to accept
   * @return the accepted household invite response
   */
  @Operation(summary = "Accept household invite",
      description = "Accepts an invitation to join a household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully accepted invite"),
      @ApiResponse(responseCode = "400", description = "Invalid invite ID or already processed"),
      @ApiResponse(responseCode = "404", description = "Invite not found")
  })
  @PostMapping("/{inviteId}/accept")
  public ResponseEntity<HouseholdInviteResponse> acceptInvite(
      @Parameter(description = "Invite ID") @PathVariable UUID inviteId) {
    return ResponseEntity.ok(inviteService.acceptInvite(inviteId));
  }

  /**
   * Declines a household invite.
   *
   * <p>This endpoint allows users to decline an existing household invite by its ID.</p>
   *
   * @param inviteId the ID of the invite to decline
   * @return the declined household invite response
   */
  @Operation(summary = "Decline household invite",
      description = "Declines an invitation to join a household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully declined invite"),
      @ApiResponse(responseCode = "400", description = "Invalid invite ID or already processed"),
      @ApiResponse(responseCode = "404", description = "Invite not found")
  })
  @PostMapping("/{inviteId}/decline")
  public ResponseEntity<HouseholdInviteResponse> declineInvite(
      @Parameter(description = "Invite ID") @PathVariable UUID inviteId) {
    return ResponseEntity.ok(inviteService.declineInvite(inviteId));
  }

  /**
   * Cancels a household invite.
   *
   * <p>This endpoint allows users to cancel an existing household invite by its ID.</p>
   *
   * @param inviteId the ID of the invite to cancel
   * @return the canceled household invite response
   */
  @Operation(summary = "Cancel household invite",
      description = "Cancels a previously sent household invitation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully canceled invite"),
      @ApiResponse(responseCode = "400", description = "Invalid invite ID or already processed"),
      @ApiResponse(responseCode = "404", description = "Invite not found")
  })
  @PostMapping("/{inviteId}/cancel")
  public ResponseEntity<HouseholdInviteResponse> cancelInvite(
      @Parameter(description = "Invite ID") @PathVariable UUID inviteId) {
    return ResponseEntity.ok(inviteService.cancelInvite(inviteId));
  }

  /**
   * Retrieves pending invites for the current user.
   *
   * <p>This endpoint allows users to fetch all pending invites associated with their account.</p>
   *
   * @return a list of pending household invite responses
   */
  @Operation(summary = "Get pending invites for current user",
      description = "Retrieves all pending household invitations for the authenticated user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved pending invites")
  })
  @GetMapping("/pending")
  public ResponseEntity<List<HouseholdInviteResponse>> getPendingInvitesForUser() {
    List<HouseholdInviteResponse> invites = inviteService.getPendingInvitesForUser();

    return ResponseEntity.ok(invites);
  }

  /**
   * Retrieves pending invites for a specific household.
   *
   * <p>This endpoint allows users with admin role to fetch all pending invites associated with a
   * specific household.</p>
   *
   * @param householdId the ID of the household to fetch invites for
   * @return a list of pending household invite responses
   */
  @Operation(summary = "Get pending invites for household",
      description = "Retrieves all pending invitations for a specific household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved pending invites"),
      @ApiResponse(responseCode = "404", description = "Household not found")
  })
  @GetMapping("/household/{householdId}/pending")
  public ResponseEntity<List<HouseholdInviteResponse>> getPendingInvitesForHousehold(
      @Parameter(description = "Household ID") @PathVariable UUID householdId) {
    return ResponseEntity.ok(inviteService.getPendingInvitesForHousehold(householdId));
  }

  /**
   * Retrieves all invites for a specific household.
   *
   * <p>This endpoint allows users with admin role to fetch all invites associated with a specific
   * household.</p>
   *
   * @param householdId the ID of the household to fetch invites for
   * @return a list of all household invite responses
   */
  @Operation(summary = "Get all invites for household (Admin only)",
      description = "Retrieves all invitations (pending, accepted, declined) for a specific "
          + "household")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved all invites"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
      @ApiResponse(responseCode = "404", description = "Household not found")
  })
  @GetMapping("/household/{householdId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<HouseholdInviteResponse>> getAllInvitesForHousehold(
      @Parameter(description = "Household ID") @PathVariable UUID householdId) {
    return ResponseEntity.ok(inviteService.getPendingInvitesForHousehold(householdId));
  }
}