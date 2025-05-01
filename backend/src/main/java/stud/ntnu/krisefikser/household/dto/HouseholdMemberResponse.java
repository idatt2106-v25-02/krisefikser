package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberResponse {

  private UserResponse user;
  private HouseholdMemberStatus status;
}
