package stud.ntnu.krisefikser.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object containing data required to create a new checklist item.
 *
 * <p>This class encapsulates all the necessary information provided by clients
 * when adding a new item to an emergency preparedness checklist.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChecklistItemRequest {

  /**
   * Name or description of the checklist item.
   */
  @NotBlank(message = "Name is required")
  private String name;

  /**
   * Icon identifier representing the checklist item visually.
   */
  @NotBlank(message = "Icon is required")
  private String icon;

  /**
   * Whether the item is initially checked or not. Default value is false.
   */
  @NotBlank(message = "Checked status is required")
  private Boolean checked = false;
}
