package stud.ntnu.krisefikser.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a request to create or update a meeting point.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPointRequest {

  private String name;
  private String description;
  private double latitude;
  private double longitude;
}