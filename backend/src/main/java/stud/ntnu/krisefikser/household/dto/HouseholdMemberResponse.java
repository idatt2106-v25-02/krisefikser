package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.user.dto.UserDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberResponse {
    private UserDto user;
    private HouseholdMemberStatus status;
}
