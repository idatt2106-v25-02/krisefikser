package stud.ntnu.krisefikser.household.data;

import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import stud.ntnu.krisefikser.user.data.UserResponse;

public class HouseholdResponse {
  private String name;
  private Point location;
  private UserResponse owner;
  private Page<HouseholdMemberResponse> members;
  private LocalDateTime createdAt;
}
