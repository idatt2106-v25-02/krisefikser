package stud.ntnu.krisefikser.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.service.MapPointService;

import java.util.List;

/**
 * REST controller for managing map points in the system.
 * Provides endpoints for CRUD operations on map points.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/map-points")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Map Point", description = "Map Point management APIs")
public class MapPointController {
    private final MapPointService mapPointService;

    /**
     * Retrieves all map points from the system.
     *
     * @return ResponseEntity containing a list of all map points.
     * @since 1.0
     */
    @Operation(summary = "Get all map points", description = "Retrieves a list of all map points in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved map points", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MapPoint.class)))
    })
    @GetMapping
    public ResponseEntity<List<MapPoint>> getAllMapPoints() {
        return ResponseEntity.ok(mapPointService.getAllMapPoints());
    }

    /**
     * Retrieves a specific map point by its ID.
     *
     * @param id The ID of the map point to retrieve.
     * @return ResponseEntity containing the requested map point.
     * @since 1.0
     */
    @Operation(summary = "Get a map point by ID", description = "Retrieves a specific map point by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the map point", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MapPoint.class))),
            @ApiResponse(responseCode = "404", description = "Map point not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MapPoint> getMapPointById(
            @Parameter(description = "ID of the map point to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(mapPointService.getMapPointById(id));
    }

    /**
     * Creates a new map point in the system.
     * Only accessible to users with ADMIN role.
     *
     * @param mapPoint The map point to create.
     * @return ResponseEntity containing the created map point.
     * @since 1.0
     */
    @Operation(summary = "Create a new map point", description = "Creates a new map point in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the map point", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MapPoint.class))),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MapPoint> createMapPoint(
            @Parameter(description = "Map point to create") @RequestBody MapPoint mapPoint) {
        return ResponseEntity.ok(mapPointService.createMapPoint(mapPoint));
    }

    /**
     * Updates an existing map point in the system.
     * Only accessible to users with ADMIN role.
     *
     * @param id       The ID of the map point to update.
     * @param mapPoint The updated map point data.
     * @return ResponseEntity containing the updated map point.
     * @since 1.0
     */
    @Operation(summary = "Update a map point", description = "Updates an existing map point by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the map point", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MapPoint.class))),
            @ApiResponse(responseCode = "404", description = "Map point not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MapPoint> updateMapPoint(
            @Parameter(description = "ID of the map point to update") @PathVariable Long id,
            @Parameter(description = "Updated map point details") @RequestBody MapPoint mapPoint) {
        return ResponseEntity.ok(mapPointService.updateMapPoint(id, mapPoint));
    }

    @Operation(summary = "Delete a map point", description = "Deletes a map point from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the map point"),
            @ApiResponse(responseCode = "404", description = "Map point not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMapPoint(
            @Parameter(description = "ID of the map point to delete") @PathVariable Long id) {
        mapPointService.deleteMapPoint(id);
        return ResponseEntity.noContent().build();
    }
}