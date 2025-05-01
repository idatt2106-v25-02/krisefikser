package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdResponse {

  @NotNull
  private UUID id;
  @NotNull
  private String name;
  @NotNull
  private double latitude;
  @NotNull
  private double longitude;
  @NotNull
  private String address;
  @NotNull
  private UserResponse owner;
  @NotNull
  private List<HouseholdMemberResponse> members;
  @NotNull
  private LocalDateTime createdAt;
  @NotNull
  private boolean isActive;
}
