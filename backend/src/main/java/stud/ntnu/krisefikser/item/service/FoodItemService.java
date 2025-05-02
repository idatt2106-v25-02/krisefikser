package stud.ntnu.krisefikser.item.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;

/**
 * Service responsible for managing food items in the emergency preparedness system.
 *
 * <p>This service handles the creation and retrieval of food items associated with
 * households. It interacts with the {@link FoodItemRepository} for persistence operations and
 * {@link HouseholdService} to determine the active household context.</p>
 */
@Service
@RequiredArgsConstructor
public class FoodItemService {

  /**
   * Repository for FoodItem entity operations.
   */
  private final FoodItemRepository foodItemRepository;

  /**
   * Service used to determine the active household context.
   */
  private final HouseholdService householdService;

  /**
   * Creates a new food item associated with the active household.
   *
   * <p>The method creates a new food item using the provided details and associates it
   * with the currently active household. The item is then persisted to the database.</p>
   *
   * @param createRequest the request object containing food item details
   * @return a response object containing the created food item's details
   * @throws RuntimeException if no active household exists
   */
  @Transactional
  public FoodItemResponse createFoodItem(CreateFoodItemRequest createRequest) {
    Household activeHousehold = householdService.getActiveHousehold();

    FoodItem newItem = FoodItem.builder()
        .name(createRequest.getName())
        .icon(createRequest.getIcon())
        .kcal(createRequest.getKcal())
        .expirationDate(createRequest.getExpirationDate())
        .household(activeHousehold)
        .build();

    return foodItemRepository.save(newItem).toResponse();
  }

  /**
   * Retrieves all food items associated with the active household.
   *
   * <p>This method fetches all food items that belong to the currently active household
   * and converts them to response DTOs.</p>
   *
   * @return a list of food item response DTOs
   * @throws RuntimeException if no active household exists
   */
  public List<FoodItemResponse> getAllFoodItems() {
    Household activeHousehold = householdService.getActiveHousehold();

    return foodItemRepository.findByHousehold(activeHousehold).stream().map(FoodItem::toResponse)
        .toList();
  }

  /**
   * Updates a food item with the specified ID.
   *
   * <p>This method updates the details of a food item identified by its unique ID
   * with the provided request details.</p>
   *
   * @param id         the unique identifier of the food item to update
   * @param putRequest the request object containing the updated food item details
   * @return a response object containing the updated food item's details
   * @throws RuntimeException if the item is not found
   */
  public FoodItemResponse updateFoodItem(String id, CreateFoodItemRequest putRequest) {
    FoodItem item = foodItemRepository.findById(UUID.fromString(id))
        .orElseThrow(() -> new RuntimeException("Item not found"));
    item.setName(putRequest.getName());
    item.setIcon(putRequest.getIcon());
    item.setKcal(putRequest.getKcal());
    item.setExpirationDate(putRequest.getExpirationDate());
    return foodItemRepository.save(item).toResponse();
  }

  /**
   * Deletes a food item with the specified ID.
   *
   * <p>This method deletes a food item identified by its unique ID from the system.</p>
   *
   * @param id the unique identifier of the food item to delete
   * @throws RuntimeException if the item is not found
   */
  public void deleteFoodItem(String id) {
    foodItemRepository.deleteById(UUID.fromString(id));
  }

}
