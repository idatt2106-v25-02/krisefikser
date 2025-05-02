package stud.ntnu.krisefikser.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;

@ExtendWith(MockitoExtension.class)
class FoodItemServiceTest {

  @Mock
  private FoodItemRepository foodItemRepository;

  @Mock
  private HouseholdService householdService;

  @InjectMocks
  private FoodItemService foodItemService;

  private CreateFoodItemRequest createRequest;
  private FoodItem foodItem;
  private Household household;
  private Instant expirationDate;

  @BeforeEach
  void setUp() {
    household = new Household();
    household.setId(UUID.randomUUID());
    household.setName("Test Household");

    expirationDate = Instant.now().plusSeconds(86400 * 365); // 1 year from now

    createRequest = new CreateFoodItemRequest(
        "Emergency Rations",
        "food_icon",
        2000,
        expirationDate
    );

    foodItem = FoodItem.builder()
        .id(UUID.randomUUID())
        .name("Emergency Rations")
        .icon("food_icon")
        .kcal(2000)
        .expirationDate(expirationDate)
        .household(household)
        .build();
  }

  @Test
  void createFoodItem_ShouldReturnCreatedItem() {
    // Arrange
    when(householdService.getActiveHousehold()).thenReturn(household);
    when(foodItemRepository.save(any(FoodItem.class))).thenReturn(foodItem);

    // Act
    FoodItemResponse result = foodItemService.createFoodItem(createRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(foodItem.getId());
    assertThat(result.getName()).isEqualTo("Emergency Rations");
    assertThat(result.getIcon()).isEqualTo("food_icon");
    assertThat(result.getKcal()).isEqualTo(2000);
    assertThat(result.getExpirationDate()).isEqualTo(expirationDate);

    verify(householdService).getActiveHousehold();
    verify(foodItemRepository).save(any(FoodItem.class));
  }

  @Test
  void createFoodItem_WhenNoActiveHousehold_ShouldThrowException() {
    // Arrange
    when(householdService.getActiveHousehold())
        .thenThrow(new HouseholdNotFoundException());

    // Act & Assert
    assertThatThrownBy(() -> foodItemService.createFoodItem(createRequest))
        .isInstanceOf(HouseholdNotFoundException.class)
        .hasMessage("Household not found");

    verify(householdService).getActiveHousehold();
  }

  @Test
  void getAllFoodItems_ShouldReturnAllItems() {
    // Arrange
    List<FoodItem> foodItems = List.of(foodItem);
    when(householdService.getActiveHousehold()).thenReturn(household);
    when(foodItemRepository.findByHousehold(household)).thenReturn(foodItems);

    // Act
    List<FoodItemResponse> result = foodItemService.getAllFoodItems();

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(foodItem.getId());
    assertThat(result.getFirst().getName()).isEqualTo("Emergency Rations");
    assertThat(result.getFirst().getIcon()).isEqualTo("food_icon");
    assertThat(result.getFirst().getKcal()).isEqualTo(2000);
    assertThat(result.getFirst().getExpirationDate()).isEqualTo(expirationDate);

    verify(householdService).getActiveHousehold();
    verify(foodItemRepository).findByHousehold(household);
  }

  @Test
  void getAllFoodItems_WhenNoItems_ShouldReturnEmptyList() {
    // Arrange
    when(householdService.getActiveHousehold()).thenReturn(household);
    when(foodItemRepository.findByHousehold(household)).thenReturn(Collections.emptyList());

    // Act
    List<FoodItemResponse> result = foodItemService.getAllFoodItems();

    // Assert
    assertThat(result).isNotNull().isEmpty();

    verify(householdService).getActiveHousehold();
    verify(foodItemRepository).findByHousehold(household);
  }

  @Test
  void getAllFoodItems_WhenNoActiveHousehold_ShouldThrowException() {
    // Arrange
    when(householdService.getActiveHousehold())
        .thenThrow(new HouseholdNotFoundException());

    // Act & Assert
    assertThatThrownBy(() -> foodItemService.getAllFoodItems())
        .isInstanceOf(HouseholdNotFoundException.class)
        .hasMessage("Household not found");

    verify(householdService).getActiveHousehold();
  }

  @Test
  void updateFoodItem_WhenItemExists_ShouldReturnUpdatedItem() {
    // Arrange
    UUID itemId = UUID.randomUUID();
    String itemIdString = itemId.toString();
    
    CreateFoodItemRequest updateRequest = new CreateFoodItemRequest(
        "Updated Rations",
        "new_icon",
        3000,
        expirationDate.plusSeconds(86400 * 365) // 1 more year
    );
    
    FoodItem existingItem = FoodItem.builder()
        .id(itemId)
        .name("Emergency Rations")
        .icon("food_icon")
        .kcal(2000)
        .expirationDate(expirationDate)
        .household(household)
        .build();
    
    FoodItem updatedItem = FoodItem.builder()
        .id(itemId)
        .name("Updated Rations")
        .icon("new_icon")
        .kcal(3000)
        .expirationDate(expirationDate.plusSeconds(86400 * 365))
        .household(household)
        .build();
    
    when(foodItemRepository.findById(itemId)).thenReturn(java.util.Optional.of(existingItem));
    when(foodItemRepository.save(any(FoodItem.class))).thenReturn(updatedItem);
    
    // Act
    FoodItemResponse result = foodItemService.updateFoodItem(itemIdString, updateRequest);
    
    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(itemId);
    assertThat(result.getName()).isEqualTo("Updated Rations");
    assertThat(result.getIcon()).isEqualTo("new_icon");
    assertThat(result.getKcal()).isEqualTo(3000);
    
    verify(foodItemRepository).findById(itemId);
    verify(foodItemRepository).save(any(FoodItem.class));
  }
  
  @Test
  void updateFoodItem_WhenItemNotFound_ShouldThrowException() {
    // Arrange
    UUID itemId = UUID.randomUUID();
    String itemIdString = itemId.toString();
    
    when(foodItemRepository.findById(itemId)).thenReturn(java.util.Optional.empty());
    
    // Act & Assert
    assertThatThrownBy(() -> foodItemService.updateFoodItem(itemIdString, createRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Item not found");
    
    verify(foodItemRepository).findById(itemId);
  }
  
  @Test
  void deleteFoodItem_ShouldDeleteItem() {
    // Arrange
    UUID itemId = UUID.randomUUID();
    String itemIdString = itemId.toString();
    
    // Act
    foodItemService.deleteFoodItem(itemIdString);
    
    // Assert
    verify(foodItemRepository).deleteById(itemId);
  }
  
  @Test
  void deleteFoodItem_WhenInvalidUUID_ShouldThrowException() {
    // Arrange
    String invalidId = "invalid-uuid";
    
    // Act & Assert
    assertThatThrownBy(() -> foodItemService.deleteFoodItem(invalidId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid UUID string");
  }
} 