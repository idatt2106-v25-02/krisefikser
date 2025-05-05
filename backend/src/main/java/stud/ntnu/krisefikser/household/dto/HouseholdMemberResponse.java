package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberResponse {

  @NotNull
  private UserResponse user;
  @NotNull
  private HouseholdMemberStatus status;
}
