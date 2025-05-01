package stud.ntnu.krisefikser.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.service.EventService;

/**
 * REST controller for managing events in the system. Provides endpoints for CRUD operations on
 * events.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Event", description = "Event management APIs")
public class EventController {

  private final EventService eventService;

  /**
   * Retrieves all events from the system.
   *
   * @return ResponseEntity containing a list of all events.
   * @since 1.0
   */
  @Operation(summary = "Get all events", description = "Retrieves a list of all events in the "
      + "system")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Successfully retrieved events",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(
                  schema = @Schema(implementation = EventResponse.class)
              )
          )
      )
  })
  @GetMapping
  public ResponseEntity<List<EventResponse>> getAllEvents() {
    return ResponseEntity.ok(eventService.getAllEvents());
  }

  /**
   * Retrieves a specific event by its ID.
   *
   * @param id The ID of the event to retrieve.
   * @return ResponseEntity containing the requested event.
   * @since 1.0
   */
  @Operation(summary = "Get an event by ID", description = "Retrieves a specific event by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the event",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              EventResponse.class))),
      @ApiResponse(responseCode = "404", description = "Event not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<EventResponse> getEventById(
      @Parameter(description = "ID of the event to retrieve") @PathVariable Long id) {
    return ResponseEntity.ok(eventService.getEventById(id));
  }

  /**
   * Creates a new event in the system. Only accessible to users with ADMIN role.
   *
   * @param event The event to create.
   * @return ResponseEntity containing the created event.
   * @since 1.0
   */
  @Operation(summary = "Create a new event", description = "Creates a new event in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully created the event", content
          = @Content(mediaType = "application/json", schema = @Schema(implementation =
          EventResponse.class))),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EventResponse> createEvent(
      @Parameter(description = "Event to create") @RequestBody Event event) {
    return ResponseEntity.ok(eventService.createEvent(event));
  }

  /**
   * Updates an existing event in the system. Only accessible to users with ADMIN role.
   *
   * @param id    The ID of the event to update.
   * @param event The updated event data.
   * @return ResponseEntity containing the updated event.
   * @since 1.0
   */
  @Operation(summary = "Update an event", description = "Updates an existing event by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated the event", content
          = @Content(mediaType = "application/json", schema = @Schema(implementation =
          EventResponse.class))),
      @ApiResponse(responseCode = "404", description = "Event not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EventResponse> updateEvent(
      @Parameter(description = "ID of the event to update") @PathVariable Long id,
      @Parameter(description = "Updated event details") @RequestBody Event event) {
    return ResponseEntity.ok(eventService.updateEvent(id, event));
  }

  /**
   * Deletes an event from the system. Only accessible to users with ADMIN role.
   *
   * @param id The ID of the event to delete.
   * @return ResponseEntity with no content.
   * @since 1.0
   */
  @Operation(summary = "Delete an event", description = "Deletes an event from the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Successfully deleted the event"),
      @ApiResponse(responseCode = "404", description = "Event not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteEvent(
      @Parameter(description = "ID of the event to delete") @PathVariable Long id) {
    eventService.deleteEvent(id);
    return ResponseEntity.noContent().build();
  }
}