package stud.ntnu.krisefikser.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChecklistItemRequest {
    private String name;
    private String icon;
    private Boolean checked = false;
}
