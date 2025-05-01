package stud.ntnu.krisefikser.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    private UUID id;
    
    /**
     * Name or description of the checklist item.
     */
    private String name;
    
    /**
     * Icon identifier representing the checklist item visually.
     */
    private String icon;
    
    /**
     * Whether the item has been checked (completed/verified) or not.
     * Default value is false.
     */
    private Boolean checked = false;
}
