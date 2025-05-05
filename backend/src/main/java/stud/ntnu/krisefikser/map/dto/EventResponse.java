package stud.ntnu.krisefikser.map.dto;

import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

/**
 * Data Transfer Object (DTO) for representing an event response. This class is used to transfer
 * data between the server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

  @NotNull
  private Long id;

  @NotNull
  private String title;

  @NotNull
  private String description;

  @NotNull
  private Double radius;

  @NotNull
  private Double latitude;

  @NotNull
  private Double longitude;

  @NotNull
  private EventLevel level;

  @NotNull
  private ZonedDateTime startTime;

  private ZonedDateTime endTime;

  @NotNull
  private EventStatus status;
}
