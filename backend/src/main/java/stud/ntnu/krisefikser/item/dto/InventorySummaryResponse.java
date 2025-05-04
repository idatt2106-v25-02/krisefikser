package stud.ntnu.krisefikser.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a checklist item in API responses.
 *
 * <p>This class encapsulates the essential properties of a checklist item
 * that should be exposed to API clients, hiding internal implementation details.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryResponse {

  @NotNull
  private Integer kcal;

  @NotNull
  private Integer kcalGoal;

  @NotNull
  private Double waterLiters;

  @NotNull
  private Double waterLitersGoal;

  @NotNull
  private Integer checkedItems;

  @NotNull
  private Integer totalItems;
}
