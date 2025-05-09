package stud.ntnu.krisefikser.household.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.reflection.entity.Reflection;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Entity representing a household.
 *
 * @since 1.0
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"members"})
public class Household {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String postalCode;

  @Column(nullable = false)
  private String city;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id")
  private User owner;

  @Column(nullable = false)
  @Builder.Default
  private Double waterLiters = 0.0;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
  private Set<HouseholdMember> members = new HashSet<>();

  @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
  private Set<Guest> guests = new HashSet<>();

  @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
  private Set<MeetingPoint> meetingPoints = new HashSet<>();

  @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
  private Set<HouseholdInvite> invites = new HashSet<>();

  @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
  private Set<ChecklistItem> checklistItems = new HashSet<>();

  @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
  private Set<FoodItem> foodItems = new HashSet<>();

  @OneToMany(mappedBy = "household")
  private Set<Notification> notifications = new HashSet<>();

  @OneToMany(mappedBy = "household")
  private Set<Reflection> reflections = new HashSet<>();

  @OneToMany(mappedBy = "activeHousehold")
  private Set<User> activeUsers = new HashSet<>();

  @PreRemove
  private void preRemove() {
    notifications.forEach(notification -> notification.setHousehold(null));
    reflections.forEach(reflection -> reflection.setHousehold(null));
  }
}