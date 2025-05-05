package stud.ntnu.krisefikser.map.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
