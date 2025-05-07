package stud.ntnu.krisefikser.map.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for representing a map point type response. This class is used to
 * transfer data between the server and client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapPointResponse {

  @NotNull
  private Long id;

  @NotNull
  private Double latitude;

  @NotNull
  private Double longitude;

  @NotNull
  private MapPointTypeResponse type;
}
