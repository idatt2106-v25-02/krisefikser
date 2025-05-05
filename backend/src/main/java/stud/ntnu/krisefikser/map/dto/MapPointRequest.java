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
public class MapPointRequest {

  @NotNull
  private Double latitude;

  @NotNull
  private Double longitude;

  @NotNull
  private Long typeId;
}
