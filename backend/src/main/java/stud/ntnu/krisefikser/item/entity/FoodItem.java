package stud.ntnu.krisefikser.item.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.notification.entity.Notification;

/**
 * Entity representing a food item stored by a household for emergency preparedness.
 *
 * <p>Food items are essential supplies for crisis situations, stored with information
 * about their nutritional value, expiration date, and other relevant details.</p>
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {

  /**
   * Unique identifier for the food item.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The household that owns this food item.
   */
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private Household household;

  /**
   * Name or description of the food item.
   */
  @Column(nullable = false)
  private String name;

  /**
   * Icon identifier representing the food item visually in the UI.
   */
  private String icon;

  /**
   * Kilocalories provided by the food item, used for nutritional planning.
   */
  @Column(nullable = false)
  private Integer kcal;

  /**
   * Date and time when the food item expires.
   */
  private Instant expirationDate;

  @OneToMany(mappedBy = "item")
  private Set<Notification> notifications = new HashSet<>();

  @PreRemove
  private void preRemove() {
    if (notifications != null && !notifications.isEmpty()) {
      notifications.forEach(notification -> notification.setEvent(null));
    }
  }

  /**
   * Converts this entity to a response DTO.
   *
   * @return a DTO containing the essential information about this food item
   */
  public FoodItemResponse toResponse() {
    return FoodItemResponse.builder()
        .id(id)
        .name(name)
        .icon(icon)
        .kcal(kcal)
        .expirationDate(expirationDate)
        .build();
  }
}