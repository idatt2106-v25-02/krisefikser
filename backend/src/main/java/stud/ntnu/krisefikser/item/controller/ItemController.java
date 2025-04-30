package stud.ntnu.krisefikser.item.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.service.ChecklistItemService;
import stud.ntnu.krisefikser.item.service.FoodItemService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing items.
 * Provides endpoints for item management operations.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "Item management APIs")
public class ItemController {
    private final FoodItemService foodItemService;
    private final ChecklistItemService checklistItemService;

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