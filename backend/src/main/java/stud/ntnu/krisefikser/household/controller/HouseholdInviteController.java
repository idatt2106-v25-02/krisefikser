package stud.ntnu.krisefikser.household.controller;

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
  @PostMapping
  public ResponseEntity<HouseholdInviteResponse> createInvite(
      @Valid @RequestBody CreateHouseholdInviteRequest request) {
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
  @PostMapping("/{inviteId}/accept")
  public ResponseEntity<HouseholdInviteResponse> acceptInvite(@PathVariable UUID inviteId) {
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
  @PostMapping("/{inviteId}/decline")
  public ResponseEntity<HouseholdInviteResponse> declineInvite(@PathVariable UUID inviteId) {
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
  @PostMapping("/{inviteId}/cancel")
  public ResponseEntity<HouseholdInviteResponse> cancelInvite(@PathVariable UUID inviteId) {
    return ResponseEntity.ok(inviteService.cancelInvite(inviteId));
  }

  /**
   * Retrieves pending invites for the current user.
   *
   * <p>This endpoint allows users to fetch all pending invites associated with their account.</p>
   *
   * @return a list of pending household invite responses
   */
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
  @GetMapping("/household/{householdId}/pending")
  public ResponseEntity<List<HouseholdInviteResponse>> getPendingInvitesForHousehold(
      @PathVariable UUID householdId) {
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
  @GetMapping("/household/{householdId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<HouseholdInviteResponse>> getAllInvitesForHousehold(
      @PathVariable UUID householdId) {
    return ResponseEntity.ok(inviteService.getPendingInvitesForHousehold(householdId));
  }
}