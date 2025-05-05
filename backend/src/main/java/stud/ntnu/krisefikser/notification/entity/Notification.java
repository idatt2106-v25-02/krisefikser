package stud.ntnu.krisefikser.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Entity class representing a notification in the system.
 * Maps to the 'notifications' table in the database and contains all
 * relevant information about user notifications.
 *
 * @since 1.0
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {
  /**
   * Unique identifier for the notification.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The user who owns this notification.
   */
  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  /**
   * The type of notification (e.g., EVENT, INVITE).
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  /**
   * The title of the notification.
   */
  @Column(nullable = false)
  private String title;

  /**
   * The content message of the notification.
   */
  @Column(nullable = false)
  private String message;

  /**
   * Optional URL associated with the notification for further actions.
   */
  private String url;

  /**
   * Indicates whether the notification has been read by the user.
   * Defaults to false when created.
   */
  @Column(nullable = false)
  private Boolean read = false;

  /**
   * Timestamp of when the notification was created.
   * Automatically set by Hibernate when the entity is persisted.
   */
  @CreationTimestamp
  private LocalDateTime createdAt;

  /**
   * Converts the Notification entity to a NotificationResponse DTO.
   *
   * @return a NotificationResponse containing all relevant notification data
   */
  public NotificationResponse toResponse() {
    return NotificationResponse.builder()
        .title(title)
        .message(message)
        .type(type)
        .url(url)
        .read(read)
        .createdAt(createdAt)
        .build();
  }
}