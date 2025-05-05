package stud.ntnu.krisefikser.household.dto;

import stud.ntnu.krisefikser.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a household's response.
 *
 * @since 1.0
 */
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
