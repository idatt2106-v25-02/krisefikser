package stud.ntnu.krisefikser.reflection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.reflection.dto.CreateReflectionRequest;
import stud.ntnu.krisefikser.reflection.dto.ReflectionResponse;
import stud.ntnu.krisefikser.reflection.dto.UpdateReflectionRequest;
import stud.ntnu.krisefikser.reflection.exception.ReflectionNotFoundException;
import stud.ntnu.krisefikser.reflection.exception.UnauthorizedReflectionAccessException;
import stud.ntnu.krisefikser.reflection.service.ReflectionService;

/**
 * REST controller for managing reflections within the crisis management system.
 *
 * <p>Reflections allow users to document their experiences and thoughts after experiencing a
 * disaster or emergency situation. They can be shared publicly, kept private, or shared only with
 * household members, depending on the chosen visibility.</p>
 *
 * <p>The controller delegates business logic to the {@link ReflectionService} component.</p>
 *
 * @author Krisefikser Development Team
 * @see ReflectionService
 * @see ReflectionResponse
 * @since 1.0
 */
@RestController
@RequestMapping("/api/reflections")
@RequiredArgsConstructor
@Tag(name = "Reflection", description = "Reflection management APIs")
public class ReflectionController {

  /**
   * Service component responsible for reflection operations.
   */
  private final ReflectionService reflectionService;

  /**
   * Creates a new reflection.
   *
   * <p>This endpoint allows authenticated users to create reflections with details such as title,
   * content, and visibility settings. Each created reflection receives a unique identifier.</p>
   *
   * @param request the DTO containing the reflection details to be created. Must not be null and
   *                should contain valid data.
   * @return ResponseEntity containing the created reflection's details with HTTP status 201
   * (Created)
   */
  @PostMapping
  @Operation(summary = "Create a new reflection")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Reflection created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<ReflectionResponse> createReflection(
      @Parameter(description = "Reflection data") @Valid @RequestBody CreateReflectionRequest request) {
    ReflectionResponse createdReflection = reflectionService.createReflection(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdReflection);
  }

  /**
   * Retrieves a reflection by its ID.
   *
   * <p>Access is restricted based on the reflection's visibility settings and the current user's
   * permissions. Public reflections are accessible to all users, while household and private
   * reflections have restricted access.</p>
   *
   * @param id the ID of the reflection to retrieve
   * @return ResponseEntity containing the requested reflection with HTTP status 200 (OK)
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get a reflection by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflection retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Reflection not found"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<ReflectionResponse> getReflectionById(
      @Parameter(description = "Reflection ID") @PathVariable UUID id) {
    ReflectionResponse reflection = reflectionService.getReflectionById(id);
    return ResponseEntity.ok(reflection);
  }

  /**
   * Updates an existing reflection.
   *
   * <p>Only the author or an admin can update a reflection. The updated details include title,
   * content, and visibility settings.</p>
   *
   * @param id      the ID of the reflection to update
   * @param request the DTO containing the updated reflection details
   * @return ResponseEntity containing the updated reflection with HTTP status 200 (OK)
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update a reflection")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflection updated successfully"),
      @ApiResponse(responseCode = "404", description = "Reflection not found"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<ReflectionResponse> updateReflection(
      @Parameter(description = "Reflection ID") @PathVariable UUID id,
      @Parameter(description = "Updated reflection data") @Valid @RequestBody UpdateReflectionRequest request) {
    ReflectionResponse updatedReflection = reflectionService.updateReflection(id, request);
    return ResponseEntity.ok(updatedReflection);
  }

  /**
   * Deletes a reflection.
   *
   * <p>Only the author or an admin can delete a reflection.</p>
   *
   * @param id the ID of the reflection to delete
   * @return ResponseEntity with HTTP status 204 (No Content)
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a reflection")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Reflection deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Reflection not found"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<Void> deleteReflection(
      @Parameter(description = "Reflection ID") @PathVariable UUID id) {
    reflectionService.deleteReflection(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Retrieves all reflections accessible to the current user.
   *
   * <p>This includes all public reflections, the user's own private reflections, and household
   * reflections for households the user belongs to.</p>
   *
   * @return ResponseEntity containing a list of accessible reflections with HTTP status 200 (OK)
   */
  @GetMapping
  @Operation(summary = "Get accessible reflections")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflections retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<List<ReflectionResponse>> getAccessibleReflections() {
    List<ReflectionResponse> reflections = reflectionService.getAccessibleReflections();
    return ResponseEntity.ok(reflections);
  }

  /**
   * Retrieves reflections authored by the current user.
   *
   * @return ResponseEntity containing a list of the user's reflections with HTTP status 200 (OK)
   */
  @GetMapping("/my")
  @Operation(summary = "Get my reflections")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflections retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<List<ReflectionResponse>> getCurrentUserReflections() {
    List<ReflectionResponse> reflections = reflectionService.getCurrentUserReflections();
    return ResponseEntity.ok(reflections);
  }

  /**
   * Retrieves public reflections.
   *
   * @return ResponseEntity containing a list of public reflections with HTTP status 200 (OK)
   */
  @GetMapping("/public")
  @Operation(summary = "Get public reflections")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflections retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<List<ReflectionResponse>> getPublicReflections() {
    List<ReflectionResponse> reflections = reflectionService.getPublicReflections();
    return ResponseEntity.ok(reflections);
  }

  /**
   * Retrieves household reflections for the current user's active household.
   *
   * @return ResponseEntity containing a list of reflections for the current user's household with
   * HTTP status 200 (OK)
   */
  @GetMapping("/household")
  @Operation(summary = "Get household reflections")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflections retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "User has no active household"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<List<ReflectionResponse>> getHouseholdReflections() {
    List<ReflectionResponse> reflections = reflectionService.getHouseholdReflections();
    return ResponseEntity.ok(reflections);
  }

  /**
   * Retrieves reflections for a specific event, accessible to the current user.
   *
   * @param eventId the ID of the event
   * @return ResponseEntity containing a list of accessible reflections for the event with HTTP
   * status 200 (OK)
   */
  @GetMapping("/event/{eventId}")
  @Operation(summary = "Get reflections by event ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflections for event retrieved "
          + "successfully"),
      @ApiResponse(responseCode = "401", description = "User not authenticated"),
      @ApiResponse(responseCode = "404", description = "Event not found or no reflections for "
          + "event")
  })
  public ResponseEntity<List<ReflectionResponse>> getReflectionsByEventId(
      @Parameter(description = "Event ID") @PathVariable Long eventId) {
    List<ReflectionResponse> reflections = reflectionService.getReflectionsByEventId(eventId);
    return ResponseEntity.ok(reflections);
  }

  /**
   * Admin method to retrieve all reflections in the system.
   *
   * @return ResponseEntity containing a list of all reflections with HTTP status 200 (OK)
   */
  @GetMapping("/admin/all")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all reflections (Admin only)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflections retrieved successfully"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<List<ReflectionResponse>> getAllReflections() {
    List<ReflectionResponse> reflections = reflectionService.getAllReflections();
    return ResponseEntity.ok(reflections);
  }

  /**
   * Admin method to update any reflection in the system, regardless of authorship.
   *
   * @param id      the ID of the reflection to update
   * @param request the DTO containing the updated reflection details
   * @return ResponseEntity containing the updated reflection with HTTP status 200 (OK)
   */
  @PutMapping("/admin/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update any reflection (Admin only)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reflection updated successfully"),
      @ApiResponse(responseCode = "404", description = "Reflection not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<ReflectionResponse> adminUpdateReflection(
      @Parameter(description = "Reflection ID") @PathVariable UUID id,
      @Parameter(description = "Updated reflection data") @Valid @RequestBody UpdateReflectionRequest request) {
    // Using the same service method as regular update, as it already checks for
    // admin privileges
    ReflectionResponse updatedReflection = reflectionService.updateReflection(id, request);
    return ResponseEntity.ok(updatedReflection);
  }

  /**
   * Admin method to delete any reflection in the system, regardless of authorship.
   *
   * @param id the ID of the reflection to delete
   * @return ResponseEntity with HTTP status 204 (No Content)
   */
  @DeleteMapping("/admin/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete any reflection (Admin only)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Reflection deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Reflection not found"),
      @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
      @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  public ResponseEntity<Void> adminDeleteReflection(
      @Parameter(description = "Reflection ID") @PathVariable UUID id) {
    // Using the same service method as regular delete, as it already checks for
    // admin privileges
    reflectionService.deleteReflection(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Handles exceptions related to reflection not found.
   *
   * @param ex the exception thrown when a reflection is not found
   * @return ResponseEntity with HTTP status 404 (Not Found) and the exception message
   */
  @ExceptionHandler(ReflectionNotFoundException.class)
  public ResponseEntity<String> handleReflectionNotFoundException(ReflectionNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  /**
   * Handles exceptions related to unauthorized access to reflections.
   *
   * @param ex the exception thrown when a user tries to access a reflection they are not authorized
   *           to view
   * @return ResponseEntity with HTTP status 403 (Forbidden) and the exception message
   */
  @ExceptionHandler(UnauthorizedReflectionAccessException.class)
  public ResponseEntity<String> handleUnauthorizedReflectionAccessException(
      UnauthorizedReflectionAccessException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
  }
}