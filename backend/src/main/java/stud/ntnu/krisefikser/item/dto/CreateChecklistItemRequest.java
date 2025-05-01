package stud.ntnu.krisefikser.item.dto;

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
    private String name;
    
    /**
     * Icon identifier representing the checklist item visually.
     */
    private String icon;
    
    /**
     * Whether the item is initially checked or not.
     * Default value is false.
     */
    private Boolean checked = false;
}
