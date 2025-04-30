package stud.ntnu.krisefikser.item.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    
}