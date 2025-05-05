package stud.ntnu.krisefikser.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.item.dto.InventorySummaryResponse;

/**
 * Service responsible for generating summary for inventory items in the emergency preparedness
 * system.
 */
@Service
@RequiredArgsConstructor
public class SummaryService {

  private static final int DAYS_GOAL = 7;
  private static final int DAILY_KCAL = 2250;
  private static final int DAILY_WATER_LITERS = 3;

  private final FoodItemService foodItemService;
  private final ChecklistItemService checklistItemService;
  private final HouseholdService householdService;

  public InventorySummaryResponse getInventorySummary() {
    return InventorySummaryResponse.builder()
        .kcal(totalKcal())
        .kcalGoal(kcalGoal())
        .waterLiters(totalWaterLiters())
        .waterLitersGoal(waterLitersGoal())
        .checkedItems(checkedItems())
        .totalItems(totalCheckedItems())
        .build();
  }

  private int totalKcal() {
    return foodItemService.getAllFoodItems().stream().reduce(
        0, (sum, item) -> sum + item.getKcal(), Integer::sum);
  }

  private int kcalGoal() {
    return DAILY_KCAL * DAYS_GOAL * totalMultiplier();
  }

  private double totalWaterLiters() {
    return householdService.getActiveHousehold().getWaterLiters();
  }

  private double waterLitersGoal() {
    return DAILY_WATER_LITERS * DAYS_GOAL * totalMultiplier();
  }

  private int checkedItems() {
    return checklistItemService.getAllChecklistItems().stream()
        .reduce(0, (sum, item) -> sum + (Boolean.TRUE.equals(item.getChecked()) ? 1 : 0),
            Integer::sum);
  }

  private int totalCheckedItems() {
    return checklistItemService.getAllChecklistItems().size();
  }

  private int totalMultiplier() {
    HouseholdResponse activeHousehold = householdService.toHouseholdResponse(
        householdService.getActiveHousehold());

    return activeHousehold.getMembers().size();
  }
}
