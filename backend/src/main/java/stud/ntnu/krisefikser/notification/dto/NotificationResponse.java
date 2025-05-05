package stud.ntnu.krisefikser.notification.dto;

import java.time.LocalDateTime;
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
   * Optional URL associated with the notification for further actions.
   */
  private String url;

  /**
   * Indicates whether the notification has been read by the recipient.
   */
  private Boolean read;

  /**
   * The timestamp when the notification was created.
   */
  private LocalDateTime createdAt;
}