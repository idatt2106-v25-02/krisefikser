package stud.ntnu.krisefikser.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object containing data required to create a new food item.
 *
 * <p>This class encapsulates all the necessary information provided by clients
 * when adding a new food item to the emergency supplies inventory.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFoodItemRequest {

  /**
   * Name or description of the food item.
   */
  @NotBlank(message = "Name is required")
  private String name;

  /**
   * Icon identifier representing the food item visually.
   */
  @NotBlank(message = "Icon is required")
  private String icon;

  /**
   * Nutritional value in kilocalories. More than zero.
   */
  @Positive
  @NotBlank(message = "Kcal is required")
  private Integer kcal;

  /**
   * Date and time when the food item expires.
   */
  private Instant expirationDate;
}
