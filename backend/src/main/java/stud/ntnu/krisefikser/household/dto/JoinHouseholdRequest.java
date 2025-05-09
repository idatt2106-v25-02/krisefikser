package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a request to join a household.
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinHouseholdRequest {

  @NotNull(message = "Household ID cannot be blank")
  private UUID householdId;
}
