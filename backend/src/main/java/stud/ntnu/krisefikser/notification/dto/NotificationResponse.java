package stud.ntnu.krisefikser.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.notification.entity.NotificationType;

/**
 * Data Transfer Object (DTO) for notification responses. Contains all necessary information
 * about a notification to be sent to the client.
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
  /**
   * The ID of the notification.
   */
  private UUID id;

  /**
   * The title of the notification.
   */
  private String title;

  /**
   * The message content of the notification.
   */
  private String message;

  /**
   * The type of notification (e.g., EVENT, INVITE).
   */
  private NotificationType type;

  /**
   * Indicates whether the recipient has read the notification.
   */
  private Boolean read;

  /**
   * The timestamp when the notification was created.
   */
  private LocalDateTime createdAt;

  /**
   * Represents the unique identifier of an item associated with the notification.
   * This field links the notification to a specific item in the system,
   * providing context for notifications related to items or their statuses.
   */
  private UUID itemId;

  /**
   * The unique identifier of the event associated with this notification.
   * This field references the event that triggered or is related to the notification.
   */
  private Long eventId;

  /**
   * Represents the unique identifier of the user associated with the notification.
   * This identifier is used to link the notification to a specific user in the context
   * of the application's notification system.
   */
  private UUID inviteId;

  /**
   * Represents the unique identifier for a household associated with the notification.
   * This field links the notification to a specific household, providing context
   * for notifications related to household activities or member interactions.
   */
  private UUID householdId;
}