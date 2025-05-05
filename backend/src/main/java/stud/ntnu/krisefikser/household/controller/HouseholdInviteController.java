package stud.ntnu.krisefikser.household.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdInviteResponse;
import stud.ntnu.krisefikser.household.service.HouseholdInviteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/household-invites")
@RequiredArgsConstructor
public class HouseholdInviteController {
    private final HouseholdInviteService inviteService;
    private final Logger log = LoggerFactory.getLogger(HouseholdInviteController.class);

    @PostMapping
    public ResponseEntity<HouseholdInviteResponse> createInvite(
            @Valid @RequestBody CreateHouseholdInviteRequest request) {
        return ResponseEntity.ok(inviteService.createInvite(request));
    }

    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<HouseholdInviteResponse> acceptInvite(@PathVariable UUID inviteId) {
        return ResponseEntity.ok(inviteService.acceptInvite(inviteId));
    }

    @PostMapping("/{inviteId}/decline")
    public ResponseEntity<HouseholdInviteResponse> declineInvite(@PathVariable UUID inviteId) {
        return ResponseEntity.ok(inviteService.declineInvite(inviteId));
    }

    @PostMapping("/{inviteId}/cancel")
    public ResponseEntity<HouseholdInviteResponse> cancelInvite(@PathVariable UUID inviteId) {
        return ResponseEntity.ok(inviteService.cancelInvite(inviteId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<HouseholdInviteResponse>> getPendingInvitesForUser() {
        List<HouseholdInviteResponse> invites = inviteService.getPendingInvitesForUser();

        return ResponseEntity.ok(invites);
    }

    @GetMapping("/household/{householdId}/pending")
    public ResponseEntity<List<HouseholdInviteResponse>> getPendingInvitesForHousehold(
            @PathVariable UUID householdId) {
        return ResponseEntity.ok(inviteService.getPendingInvitesForHousehold(householdId));
    }

    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HouseholdInviteResponse>> getAllInvitesForHousehold(
            @PathVariable UUID householdId) {
        return ResponseEntity.ok(inviteService.getPendingInvitesForHousehold(householdId));
    }
}