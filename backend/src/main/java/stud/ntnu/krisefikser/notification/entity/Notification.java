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
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.map.entity.Event;
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
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String message;

  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private Boolean isRead = false;
  @CreationTimestamp
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "item_id")
  private FoodItem item;

  @ManyToOne
  @JoinColumn(name = "event_id")
  private Event event;

  @ManyToOne
  @JoinColumn(name = "household_invite_id")
  private HouseholdInvite invite;

  @ManyToOne
  @JoinColumn(name = "household_id")
  private Household household;

  /**
   * Copy constructor that creates a duplicate of the original notification
   * but allows setting a different user.
   *
   * @param original the notification to copy
   * @param user     the user to set for the new notification (can be null)
   */
  public Notification(Notification original, User user) {
    this.type = original.getType();
    this.title = original.getTitle();
    this.message = original.getMessage();
    this.isRead = original.getIsRead();
    this.createdAt =
        original.getCreatedAt() != null ? original.getCreatedAt() : LocalDateTime.now();
    this.item = original.getItem();
    this.event = original.getEvent();
    this.invite = original.getInvite();
    this.household = original.getHousehold();
    this.user = user;
  }

  /**
   * Converts the Notification entity to a NotificationResponse DTO.
   * Safely handles null references to related entities.
   *
   * @return a NotificationResponse containing all relevant notification data
   */
  public NotificationResponse toResponse() {
    return NotificationResponse.builder().id(id).title(title).message(message).type(type)
        .read(this.isRead).createdAt(createdAt).itemId(item != null ? item.getId() : null)
        .eventId(event != null ? event.getId() : null)
        .inviteId(invite != null ? invite.getId() : null)
        .householdId(household != null ? household.getId() : null).build();
  }
}