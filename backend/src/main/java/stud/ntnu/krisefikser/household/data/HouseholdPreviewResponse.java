package stud.ntnu.krisefikser.household.data;

import java.util.UUID;
import org.locationtech.jts.geom.Point;
import stud.ntnu.krisefikser.user.data.UserResponse;

public class HouseholdPreviewResponse {
  private UUID id;
  private String name;
  private Point location;
  private UserResponse owner;
}
