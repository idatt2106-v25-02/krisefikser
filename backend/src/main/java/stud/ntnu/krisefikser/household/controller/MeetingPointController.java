package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.household.dto.MeetingPointRequest;
import stud.ntnu.krisefikser.household.dto.MeetingPointResponse;
import stud.ntnu.krisefikser.household.service.MeetingPointService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/households/{householdId}/meeting-points")
@RequiredArgsConstructor
@Tag(name = "Meeting Points", description = "Meeting point management APIs")
public class MeetingPointController {
        private final MeetingPointService meetingPointService;

        @Operation(summary = "Create meeting point", description = "Creates a new meeting point for the household")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Successfully created meeting point"),
                        @ApiResponse(responseCode = "400", description = "Invalid meeting point data")
        })
        @PostMapping
        public ResponseEntity<MeetingPointResponse> createMeetingPoint(
                        @Parameter(description = "Household ID") @PathVariable UUID householdId,
                        @Parameter(description = "Meeting point data") @RequestBody MeetingPointRequest request) {
                return ResponseEntity.status(201).body(meetingPointService.createMeetingPoint(householdId, request));
        }

        @Operation(summary = "Get all meeting points", description = "Retrieves all meeting points for the household")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved meeting points")
        })
        @GetMapping
        public ResponseEntity<List<MeetingPointResponse>> getMeetingPoints(
                        @Parameter(description = "Household ID") @PathVariable UUID householdId) {
                return ResponseEntity.ok(meetingPointService.getMeetingPointsByHousehold(householdId));
        }

        @Operation(summary = "Update meeting point", description = "Updates an existing meeting point")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully updated meeting point"),
                        @ApiResponse(responseCode = "404", description = "Meeting point not found")
        })
        @PutMapping("/{id}")
        public ResponseEntity<MeetingPointResponse> updateMeetingPoint(
                        @Parameter(description = "Household ID") @PathVariable UUID householdId,
                        @Parameter(description = "Meeting point ID") @PathVariable UUID id,
                        @Parameter(description = "Meeting point data") @RequestBody MeetingPointRequest request) {
                return ResponseEntity.ok(meetingPointService.updateMeetingPoint(id, request));
        }

        @Operation(summary = "Delete meeting point", description = "Deletes a meeting point")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully deleted meeting point"),
                        @ApiResponse(responseCode = "404", description = "Meeting point not found")
        })
        @DeleteMapping("/{id}")
        public void deleteMeetingPoint(
                        @Parameter(description = "Household ID") @PathVariable UUID householdId,
                        @Parameter(description = "Meeting point ID") @PathVariable UUID id) {
                meetingPointService.deleteMeetingPoint(id);
        }
}