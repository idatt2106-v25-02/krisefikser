package stud.ntnu.krisefikser.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object representing a food item in API responses.
 * 
 * <p>This class encapsulates the essential properties of a food item
 * that should be exposed to API clients, hiding internal implementation details.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {
    /**
     * Unique identifier of the food item.
     */
    private UUID id;
    
    /**
     * Name or description of the food item.
     */
    private String name;
    
    /**
     * Icon identifier representing the food item visually.
     */
    private String icon;
    
    /**
     * Nutritional value in kilocalories.
     */
    private Integer kcal;
    
    /**
     * Date and time when the food item expires.
     */
    private Instant expirationDate;
}
