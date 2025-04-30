package stud.ntnu.krisefikser.household.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinHouseholdRequest {

  private UUID householdId;
}
