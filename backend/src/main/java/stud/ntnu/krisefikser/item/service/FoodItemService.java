package stud.ntnu.krisefikser.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodItemService {
    private final FoodItemRepository foodItemRepository;
    private final HouseholdService householdService;

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

    public List<FoodItemResponse> getAllFoodItems() {
        Household activeHousehold = householdService.getActiveHousehold();

        return foodItemRepository.findByHousehold(activeHousehold).stream().map(FoodItem::toResponse).toList();
    }

}
