package stud.ntnu.krisefikser.household.data;

import java.time.LocalDateTime;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberRole;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.user.data.UserResponse;

public class HouseholdMemberResponse {
  private UserResponse user;
  private HouseholdMemberStatus status;
  private HouseholdMemberRole role;
  private LocalDateTime memberSince;
}
