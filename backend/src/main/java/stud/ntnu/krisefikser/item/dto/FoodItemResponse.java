package stud.ntnu.krisefikser.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {
    private UUID id;
    private String name;
    private String icon;
    private Integer kcal;
    private Instant expirationDate;
}
