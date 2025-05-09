package stud.ntnu.krisefikser.scenario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.scenario.dto.ScenarioResponse;

/**
 * Entity representing an emergency scenario in the crisis management system.
 *
 * <p>Scenarios contain information about different types of emergency situations,
 * providing guidance and reference material for users of the system.
 * </p>
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Scenario {

  /**
   * Unique identifier for the scenario.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The title of the scenario.
   */
  @Column(nullable = false)
  private String title;

  /**
   * The content of the scenario, typically in Markdown format.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /**
   * Converts this entity to a response DTO.
   *
   * @return a DTO containing the information about this scenario
   */
  public ScenarioResponse toResponse() {
    return ScenarioResponse.builder()
        .id(id)
        .title(title)
        .content(content)
        .build();
  }
}