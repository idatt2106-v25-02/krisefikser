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
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.service.ItemService;

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
    private final ItemService itemService;

    @PostMapping("/food")
    @Operation(summary = "Create a new food item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Food item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<FoodItemResponse> createFoodItem(
            @Parameter(description = "Food item data") @RequestBody CreateFoodItemRequest createRequest) {
        FoodItemResponse createdItem = itemService.createFoodItem(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @GetMapping("/food")
    @Operation(summary = "Get all food items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Food items retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No food items found")
    })
    public ResponseEntity<List<FoodItemResponse>> getAllFoodItems() {
        List<FoodItemResponse> foodItems = itemService.getAllFoodItems();
        return ResponseEntity.ok(foodItems);
    }
}