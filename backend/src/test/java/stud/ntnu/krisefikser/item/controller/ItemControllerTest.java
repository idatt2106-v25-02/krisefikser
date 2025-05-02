package stud.ntnu.krisefikser.item.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.service.ChecklistItemService;
import stud.ntnu.krisefikser.item.service.FoodItemService;

@WebMvcTest(controllers = ItemController.class)
@Import(TestSecurityConfig.class)
class ItemControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private FoodItemService foodItemService;

  @MockitoBean
  private ChecklistItemService checklistItemService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @MockitoBean
  private TokenService tokenService;

  private CreateFoodItemRequest createFoodItemRequest;
  private FoodItemResponse foodItemResponse;
  private ChecklistItemResponse checklistItemResponse;
  private UUID validId;

  @BeforeEach
  void setUp() {
    validId = UUID.randomUUID();

    // Use the TestDataFactory to create test objects
    createFoodItemRequest = TestDataFactory.createTestFoodItemRequest(
        "Emergency Rations",
        2000,
        Instant.now().plusSeconds(86400 * 365) // 1 year from now
    );

    foodItemResponse = TestDataFactory.createTestFoodItemResponse(
        validId,
        "Emergency Rations",
        2000,
        Instant.now().plusSeconds(86400 * 365)
    );

    checklistItemResponse = TestDataFactory.createTestChecklistItemResponse(
        validId,
        "First Aid Kit",
        "health_icon",
        false
    );
  }

  @Test
  @WithMockUser
  void createFoodItem_whenAuthenticated_shouldReturnCreatedItem() throws Exception {
    // Arrange
    when(foodItemService.createFoodItem(any(CreateFoodItemRequest.class)))
        .thenReturn(foodItemResponse);

    // Act & Assert
    mockMvc.perform(post("/api/items/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createFoodItemRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(validId.toString()))
        .andExpect(jsonPath("$.name").value("Emergency Rations"))
        .andExpect(jsonPath("$.icon").value("food_icon"))
        .andExpect(jsonPath("$.kcal").value(2000));
  }

  @Test
  void createFoodItem_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/api/items/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createFoodItemRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void getAllFoodItems_whenAuthenticated_shouldReturnItemsList() throws Exception {
    // Arrange
    List<FoodItemResponse> items = List.of(foodItemResponse);
    when(foodItemService.getAllFoodItems()).thenReturn(items);

    // Act & Assert
    mockMvc.perform(get("/api/items/food"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(validId.toString()))
        .andExpect(jsonPath("$[0].name").value("Emergency Rations"))
        .andExpect(jsonPath("$[0].icon").value("food_icon"))
        .andExpect(jsonPath("$[0].kcal").value(2000));
  }

  @Test
  @WithMockUser
  void getAllFoodItems_whenNoItems_shouldReturnEmptyList() throws Exception {
    // Arrange
    when(foodItemService.getAllFoodItems()).thenReturn(Collections.emptyList());

    // Act & Assert
    mockMvc.perform(get("/api/items/food"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getAllFoodItems_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/items/food"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void toggleChecklistItem_whenItemExists_shouldReturnUpdatedItem() throws Exception {
    // Arrange
    ChecklistItemResponse toggledItem = TestDataFactory.createTestChecklistItemResponse(
        validId, "First Aid Kit", "health_icon", true
    );

    when(checklistItemService.toggleChecklistItem(validId)).thenReturn(toggledItem);

    // Act & Assert
    mockMvc.perform(put("/api/items/checklist/" + validId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(validId.toString()))
        .andExpect(jsonPath("$.name").value("First Aid Kit"))
        .andExpect(jsonPath("$.checked").value(true));
  }

  @Test
  @WithMockUser
  void toggleChecklistItem_whenItemNotFound_shouldReturnNotFound() throws Exception {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();
    when(checklistItemService.toggleChecklistItem(nonExistentId))
        .thenThrow(
            new EntityNotFoundException("Checklist item not found with id: " + nonExistentId));

    // Act & Assert
    mockMvc.perform(put("/api/items/checklist/" + nonExistentId))
        .andExpect(status().isNotFound());
  }

  @Test
  void toggleChecklistItem_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(put("/api/items/checklist/" + validId))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void getAllChecklistItems_whenAuthenticated_shouldReturnItemsList() throws Exception {
    // Arrange
    List<ChecklistItemResponse> items = List.of(checklistItemResponse);
    when(checklistItemService.getAllChecklistItems()).thenReturn(items);

    // Act & Assert
    mockMvc.perform(get("/api/items/checklist"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(validId.toString()))
        .andExpect(jsonPath("$[0].name").value("First Aid Kit"))
        .andExpect(jsonPath("$[0].icon").value("health_icon"))
        .andExpect(jsonPath("$[0].checked").value(false));
  }

  @Test
  @WithMockUser
  void getAllChecklistItems_whenNoItems_shouldReturnEmptyList() throws Exception {
    // Arrange
    when(checklistItemService.getAllChecklistItems()).thenReturn(Collections.emptyList());

    // Act & Assert
    mockMvc.perform(get("/api/items/checklist"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getAllChecklistItems_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/items/checklist"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void updateFoodItem_whenAuthenticated_shouldReturnUpdatedItem() throws Exception {
    // Arrange
    String itemId = validId.toString();
    FoodItemResponse updatedResponse = TestDataFactory.createTestFoodItemResponse(
        validId,
        "Updated Rations",
        3000,
        Instant.now().plusSeconds(86400 * 730) // 2 years from now
    );

    when(foodItemService.updateFoodItem(itemId, createFoodItemRequest))
        .thenReturn(updatedResponse);

    // Act & Assert
    mockMvc.perform(put("/api/items/food/" + itemId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createFoodItemRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(validId.toString()))
        .andExpect(jsonPath("$.name").value("Updated Rations"))
        .andExpect(jsonPath("$.icon").value("food_icon"))
        .andExpect(jsonPath("$.kcal").value(3000));
  }

  @Test
  @WithMockUser
  void updateFoodItem_whenItemNotFound_shouldReturnNotFound() throws Exception {
    // Arrange
    String nonExistentId = UUID.randomUUID().toString();
    when(foodItemService.updateFoodItem(nonExistentId, createFoodItemRequest))
        .thenThrow(new EntityNotFoundException("Food item not found with id: " + nonExistentId));

    // Act & Assert
    mockMvc.perform(put("/api/items/food/" + nonExistentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createFoodItemRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateFoodItem_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(put("/api/items/food/" + validId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createFoodItemRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void deleteFoodItem_whenAuthenticated_shouldReturnNoContent() throws Exception {
    // Arrange
    String itemId = validId.toString();

    // Act & Assert
    mockMvc.perform(delete("/api/items/food/" + itemId))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser
  void deleteFoodItem_whenItemNotFound_shouldReturnNotFound() throws Exception {
    // Arrange
    String nonExistentId = UUID.randomUUID().toString();
    doThrow(new EntityNotFoundException("Food item not found with id: " + nonExistentId))
        .when(foodItemService).deleteFoodItem(nonExistentId);

    // Act & Assert
    mockMvc.perform(delete("/api/items/food/" + nonExistentId))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteFoodItem_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/items/food/" + validId))
        .andExpect(status().isUnauthorized());
  }
} 