package stud.ntnu.krisefikser.item.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  @NotNull
  private UUID id;

  /**
   * Name or description of the food item.
   */
  @NotNull
  private String name;

  /**
   * Icon identifier representing the food item visually.
   */
  @NotNull
  private String icon;

  /**
   * Nutritional value in kilocalories.
   */
  @NotNull
  private Integer kcal;

  /**
   * Date and time when the food item expires.
   */
  @NotNull
  private Instant expirationDate;
}
