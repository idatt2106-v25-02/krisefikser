package stud.ntnu.krisefikser.scenario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new scenario.
 *
 * <p>This class defines the required fields for creating a new scenario in the
 * system. It includes validation constraints to ensure data integrity.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScenarioRequest {

  /**
   * Title of the scenario.
   */
  @NotBlank(message = "Title is required")
  private String title;

  /**
   * Content of the scenario in Markdown format.
   */
  @NotBlank(message = "Content is required")
  private String content;
}