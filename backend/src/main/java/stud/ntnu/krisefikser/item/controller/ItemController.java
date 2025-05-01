package stud.ntnu.krisefikser.item.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.service.ChecklistItemService;
import stud.ntnu.krisefikser.item.service.FoodItemService;

/**
 * REST controller for managing emergency items within the crisis management system.
 *
 * <p>This controller provides endpoints for tracking and managing two types of items:
 * <ul>
 *   <li>Food items: Essential food supplies for crisis situations</li>
 *   <li>Checklist items: Important tasks or items to verify during emergency preparedness</li>
 * </ul>
 *
 * <p>The controller delegates business logic to the appropriate service components:
 * {@link FoodItemService} for food-related operations and {@link ChecklistItemService} for
 * checklist operations.</p>
 *
 * @author Krisefikser Development Team
 * @see FoodItemService
 * @see ChecklistItemService
 * @see FoodItemResponse
 * @see ChecklistItemResponse
 * @since 1.0
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "Item management APIs")
public class ItemController {

  /**
   * Service component responsible for food item operations. Handles creation, retrieval, and
   * management of food items.
   */
  private final FoodItemService foodItemService;

  /**
   * Service component responsible for checklist item operations. Handles status toggling and
   * retrieval of emergency checklist items.
   */
  private final ChecklistItemService checklistItemService;

  /**
   * Creates a new food item in the system.
   *
   * <p>This endpoint allows creation of food items with details such as name,
   * quantity, expiration date, and storage location. Each created food item receives a unique
   * identifier.</p>
   *
   * @param createRequest the DTO containing the food item details to be created. Must not be null
   *                      and should contain valid data.
   * @return ResponseEntity containing the created food item's details with HTTP status 201
   * (Created)
   * @throws IllegalArgumentException if the request contains invalid data
   * @throws NullPointerException     if the request is null
   * @see CreateFoodItemRequest
   * @see FoodItemResponse
   */
  @PostMapping("/food")
  @Operation(summary = "Create a new food item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Food item created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  public ResponseEntity<FoodItemResponse> createFoodItem(
      @Parameter(description = "Food item data") @RequestBody CreateFoodItemRequest createRequest) {
    FoodItemResponse createdItem = foodItemService.createFoodItem(createRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
  }

  /**
   * Retrieves all food items currently stored in the system.
   *
   * <p>This endpoint returns a comprehensive list of all food items, including their
   * identifiers, names, quantities, expiration dates, and storage locations. The list is ordered
   * based on service-defined criteria (typically by creation date or name).</p>
   *
   * @return ResponseEntity containing a list of all food items with HTTP status 200 (OK), or status
   * 404 (Not Found) if no items exist
   * @see FoodItemResponse
   */
  @GetMapping("/food")
  @Operation(summary = "Get all food items")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Food items retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "No food items found")
  })
  public ResponseEntity<List<FoodItemResponse>> getAllFoodItems() {
    List<FoodItemResponse> foodItems = foodItemService.getAllFoodItems();
    return ResponseEntity.ok(foodItems);
  }

  /**
   * Toggles the completion status of a specific checklist item.
   *
   * <p>This endpoint allows users to mark checklist items as completed or incomplete.
   * The toggle operation switches the current status to its opposite state. For instance, if an
   * item is currently marked as incomplete, this endpoint will mark it as complete, and vice
   * versa.</p>
   *
   * @param id the unique identifier of the checklist item to toggle. Must be a valid UUID that
   *           exists in the system.
   * @return ResponseEntity containing the updated checklist item with the new status, with HTTP
   * status 200 (OK)
   * @throws IllegalArgumentException  if the provided ID is invalid
   * @throws EntityNotFoundException if no checklist item with the specified ID exists
   * @see ChecklistItemResponse
   */
  @PutMapping("/checklist/{id}")
  @Operation(summary = "Toggle checklist item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Checklist item toggled successfully"),
      @ApiResponse(responseCode = "404", description = "Checklist item not found")
  })
  public ResponseEntity<ChecklistItemResponse> toggleChecklistItem(
      @Parameter(description = "ID of the checklist item to toggle") @PathVariable("id") UUID id) {
    ChecklistItemResponse checkedItem = checklistItemService.toggleChecklistItem(id);
    return ResponseEntity.ok(checkedItem);
  }

  /**
   * Retrieves all checklist items in the system.
   *
   * <p>This endpoint returns a comprehensive list of all checklist items, including their
   * identifiers, descriptions, and completion statuses. The list provides a complete view of all
   * emergency preparation tasks or items that should be verified during crisis situations. The
   * ordering is determined by the service implementation, typically prioritizing important or
   * incomplete items.</p>
   *
   * @return ResponseEntity containing a list of all checklist items with HTTP status 200 (OK), or
   * status 404 (Not Found) if no checklist items exist
   * @see ChecklistItemResponse
   */
  @GetMapping("/checklist")
  @Operation(summary = "Get all checklist items")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Checklist items retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "No checklist items found")
  })
  public ResponseEntity<List<ChecklistItemResponse>> getAllChecklistItems() {
    List<ChecklistItemResponse> checklistItems = checklistItemService.getAllChecklistItems();
    return ResponseEntity.ok(checklistItems);
  }
}
