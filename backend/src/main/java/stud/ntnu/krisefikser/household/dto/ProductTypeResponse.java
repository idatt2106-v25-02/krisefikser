package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeResponse {
    private UUID id;
    private String name;
    private String unit;
}