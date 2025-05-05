package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinHouseholdRequest {

  @NotBlank(message = "Household ID cannot be blank")
  private UUID householdId;
}
