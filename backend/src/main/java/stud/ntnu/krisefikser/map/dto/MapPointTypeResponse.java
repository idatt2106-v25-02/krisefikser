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
public class MapPointTypeResponse {

  @NotNull
  private Long id;

  @NotNull
  private String title;

  @NotNull
  private String iconUrl;

  private String description;

  private String openingTime;
}
