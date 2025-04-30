package stud.ntnu.krisefikser.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {
    private String id;
    private String name;
    private String icon;
    private Integer kcal;
    private Instant expirationDate;
}
