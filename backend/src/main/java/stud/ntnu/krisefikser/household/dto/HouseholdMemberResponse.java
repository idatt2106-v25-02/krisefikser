package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.user.dto.UserResponse;

/**
 * DTO for representing a household member's response.
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberResponse {

  private UserResponse user;
  private HouseholdMemberStatus status;
}
