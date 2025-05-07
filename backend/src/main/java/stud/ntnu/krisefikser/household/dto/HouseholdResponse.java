package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.user.dto.UserResponse;

/**
 * DTO for representing a household's response.
 *
 * @since 1.0
 */
@Data
@Builder
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
  private String postalCode;
  @NotNull
  private String city;
  @NotNull
  private UserResponse owner;
  @NotNull
  private List<HouseholdMemberResponse> members;
  @NotNull
  private List<GuestResponse> guests;
  @NotNull
  private LocalDateTime createdAt;
  @NotNull
  private boolean isActive;
}
