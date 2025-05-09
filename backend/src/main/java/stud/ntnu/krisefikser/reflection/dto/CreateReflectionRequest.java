package stud.ntnu.krisefikser.reflection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;

/**
 * Data Transfer Object for creating a new reflection.
 *
 * <p>Contains the required fields for creating a reflection, including title, content, and
 * visibility settings. The household ID is optional and only required when visibility is set to
 * HOUSEHOLD.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReflectionRequest {

  /**
   * Title of the reflection. Must not be blank.
   */
  @NotBlank(message = "Title is required")
  private String title;

  /**
   * Content of the reflection. Must not be blank.
   */
  @NotBlank(message = "Content is required")
  private String content;

  /**
   * Visibility setting of the reflection. Must not be null.
   */
  @NotNull(message = "Visibility is required")
  private VisibilityType visibility;

  /**
   * ID of the household associated with the reflection. Required only when visibility is set to
   * HOUSEHOLD.
   */
  private UUID householdId;

  /**
   * ID of the event associated with the reflection. Optional.
   */
  private Long eventId;
}