package stud.ntnu.krisefikser.scenario.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a scenario in API responses.
 *
 * <p>This class encapsulates the essential properties of a scenario that should be exposed to API
 * clients, hiding internal implementation details.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResponse {

  /**
   * Unique identifier of the scenario.
   */
  @NotNull
  private UUID id;

  /**
   * Title of the scenario.
   */
  @NotNull
  private String title;

  /**
   * Content of the scenario in Markdown format.
   */
  @NotNull
  private String content;
}