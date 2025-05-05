package stud.ntnu.krisefikser.map.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

/**
 * Data Transfer Object (DTO) for creating an event. This class is used to transfer data between the
 * server and client.
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

  @NotBlank
  private String title;

  @NotBlank
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
