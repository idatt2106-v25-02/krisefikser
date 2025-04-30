package stud.ntnu.krisefikser.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItemResponse {
    private UUID id;
    private String name;
    private String icon;
    private Boolean checked = false;
}
