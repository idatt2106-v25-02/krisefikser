package stud.ntnu.krisefikser.item.service;

import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;

import java.util.List;

@Service
public class ItemService {
    public FoodItemResponse createFoodItem(CreateFoodItemRequest createRequest) {
        return new FoodItemResponse(
                "1",
                createRequest.getName(),
                createRequest.getIcon(),
                createRequest.getKcal(),
                createRequest.getExpirationDate()
        );
    }

    public List<FoodItemResponse> getAllFoodItems() {
        return List.of(
                new FoodItemResponse("1", "Apple", "apple_icon.png", 52, null),
                new FoodItemResponse("2", "Banana", "banana_icon.png", 89, null)
        );
    }
}
