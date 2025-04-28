package stud.ntnu.krisefikser.household.data;

import java.time.LocalDateTime;
import java.util.List;

import org.locationtech.jts.geom.Point;
import stud.ntnu.krisefikser.user.data.UserResponse;

public class HouseholdResponse {
  private String name;
  private Point location;
  private UserResponse owner;
  private List<HouseholdMemberResponse> members;
  private LocalDateTime createdAt;
}
