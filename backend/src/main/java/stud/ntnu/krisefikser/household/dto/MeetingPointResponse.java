package stud.ntnu.krisefikser.household.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a meeting point's response.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPointResponse {

  private UUID id;
  private String name;
  private String description;
  private double latitude;
  private double longitude;
  private UUID householdId;
}