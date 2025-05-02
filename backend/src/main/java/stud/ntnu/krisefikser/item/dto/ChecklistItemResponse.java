package stud.ntnu.krisefikser.item.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
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
public class ChecklistItemResponse {

  /**
   * Unique identifier of the checklist item.
   */
  @NotNull
  private UUID id;

  /**
   * Name or description of the checklist item.
   */
  @NotNull
  private String name;

  /**
   * Icon identifier representing the checklist item visually.
   */
  @NotNull
  private String icon;

  /**
   * Whether the item has been checked (completed/verified) or not. Default value is false.
   */
  @NotNull
  private Boolean checked = false;
}
