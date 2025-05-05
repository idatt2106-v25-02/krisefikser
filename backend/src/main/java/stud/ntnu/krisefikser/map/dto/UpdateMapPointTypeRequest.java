package stud.ntnu.krisefikser.map.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating a map point type. This class is used to transfer data
 * between the server and client.
 *
 * @since 1.0
 */
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
