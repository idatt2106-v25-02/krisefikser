package stud.ntnu.krisefikser.map.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMapPointRequest {

  private Double latitude;
  private Double longitude;
  private Long typeId;
}
