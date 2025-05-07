package stud.ntnu.krisefikser.map.dto;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

/**
 * Data Transfer Object (DTO) for updating an event. This class is used to transfer data between the
 * server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {
  private String title;
  private String description;
  private Double radius;
  private Double latitude;
  private Double longitude;
  private EventLevel level;
  private ZonedDateTime startTime;
  private ZonedDateTime endTime;
  private EventStatus status;
}
