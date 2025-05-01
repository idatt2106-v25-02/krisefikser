package stud.ntnu.krisefikser.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.service.MapPointTypeService;

/**
 * REST controller for managing map point types in the system. Provides endpoints for CRUD
 * operations on map point types.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/map-point-types")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Map Point Type", description = "Map Point Type management APIs")
public class MapPointTypeController {

  private final MapPointTypeService mapPointTypeService;

  /**
   * Retrieves all map point types from the system.
   *
   * @return ResponseEntity containing a list of all map point types.
   * @since 1.0
   */
  @Operation(summary = "Get all map point types", description = "Retrieves a list of all map "
      + "point types in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved map point types",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              MapPointTypeResponse.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required",
          content = @Content)
  })
  @GetMapping
  public ResponseEntity<List<MapPointTypeResponse>> getAllMapPointTypes() {
    return ResponseEntity.ok(mapPointTypeService.getAllMapPointTypes());
  }

  /**
   * Retrieves a specific map point type by its ID.
   *
   * @param id The ID of the map point type to retrieve.
   * @return ResponseEntity containing the requested map point type.
   * @since 1.0
   */
  @Operation(summary = "Get a map point type by ID", description = "Retrieves a specific map "
      + "point type by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the map point type",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              MapPointTypeResponse.class))),
      @ApiResponse(responseCode = "404", description = "Map point type not found", content =
      @Content),
      @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required",
          content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<MapPointTypeResponse> getMapPointTypeById(
      @Parameter(description = "ID of the map point type to retrieve") @PathVariable Long id) {
    return ResponseEntity.ok(mapPointTypeService.getMapPointTypeById(id));
  }

  /**
   * Creates a new map point type in the system. Only accessible to users with ADMIN role.
   *
   * @param mapPointType The map point type to create.
   * @return ResponseEntity containing the created map point type.
   * @since 1.0
   */
  @Operation(summary = "Create a new map point type", description = "Creates a new map point type"
      + " in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created the map point type",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              MapPointTypeResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid map point type data provided",
          content = @Content),
      @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required",
          content = @Content),
      @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
          content = @Content)
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MapPointTypeResponse> createMapPointType(
      @Parameter(description = "Map point type to create") @RequestBody MapPointType mapPointType) {
    MapPointTypeResponse createdType = mapPointTypeService.createMapPointType(mapPointType);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdType);
  }

  /**
   * Updates an existing map point type in the system. Only accessible to users with ADMIN role.
   *
   * @param id           The ID of the map point type to update.
   * @param mapPointType The updated map point type data.
   * @return ResponseEntity containing the updated map point type.
   * @since 1.0
   */
  @Operation(summary = "Update a map point type", description = "Updates an existing map point "
      + "type by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated the map point type",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              MapPointTypeResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid map point type data provided",
          content = @Content),
      @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required",
          content = @Content),
      @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
          content = @Content),
      @ApiResponse(responseCode = "404", description = "Map point type not found", content =
      @Content)
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MapPointTypeResponse> updateMapPointType(
      @Parameter(description = "ID of the map point type to update") @PathVariable Long id,
      @Parameter(
          description = "Updated map point type details") @RequestBody MapPointType mapPointType
  ) {
    return ResponseEntity.ok(mapPointTypeService.updateMapPointType(id, mapPointType));
  }

  /**
   * Deletes a map point type from the system. Only accessible to users with ADMIN role.
   *
   * @param id The ID of the map point type to delete.
   * @return ResponseEntity with no content on successful deletion.
   * @since 1.0
   */
  @Operation(summary = "Delete a map point type", description = "Deletes a map point type from "
      + "the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Successfully deleted the map point type",
          content = @Content),
      @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required",
          content = @Content),
      @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
          content = @Content),
      @ApiResponse(responseCode = "404", description = "Map point type not found", content =
      @Content)
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteMapPointType(
      @Parameter(description = "ID of the map point type to delete") @PathVariable Long id) {
    mapPointTypeService.deleteMapPointType(id);
    return ResponseEntity.noContent().build();
  }
}