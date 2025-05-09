package stud.ntnu.krisefikser.reflection.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;

/**
 * Data Transfer Object for returning reflection information to clients.
 *
 * <p>Contains all the fields necessary to display a reflection in the UI, including author
 * information, content, and visibility settings.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionResponse {

  /**
   * Unique identifier for the reflection.
   */
  private UUID id;

  /**
   * Title of the reflection.
   */
  private String title;

  /**
   * Content of the reflection.
   */
  private String content;

  /**
   * ID of the user who authored the reflection.
   */
  private UUID authorId;

  /**
   * Name of the user who authored the reflection, typically firstName + lastName.
   */
  private String authorName;

  /**
   * Visibility setting of the reflection (PUBLIC, HOUSEHOLD, or PRIVATE).
   */
  private VisibilityType visibility;

  /**
   * ID of the household associated with the reflection (if visibility is HOUSEHOLD).
   */
  private UUID householdId;

  /**
   * Name of the household associated with the reflection (if visibility is HOUSEHOLD).
   */
  private String householdName;

  /**
   * ID of the event associated with the reflection (if any).
   */
  private Long eventId;

  /**
   * Date and time when the reflection was created.
   */
  private LocalDateTime createdAt;

  /**
   * Date and time when the reflection was last updated.
   */
  private LocalDateTime updatedAt;
}