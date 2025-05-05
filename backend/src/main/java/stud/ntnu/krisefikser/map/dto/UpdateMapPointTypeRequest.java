package stud.ntnu.krisefikser.map.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMapPointTypeRequest {

  private String title;
  private String iconUrl;
  private String description;
  private String openingTime;
}
