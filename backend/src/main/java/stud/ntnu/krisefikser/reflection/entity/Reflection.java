package stud.ntnu.krisefikser.reflection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Entity representing a user's reflection after a disaster.
 *
 * <p>Reflections allow users to document their experiences and thoughts after experiencing a
 * disaster or emergency situation. They can be shared publicly, kept private, or shared only with
 * household members, depending on the chosen visibility.
 * </p>
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reflection {

  /**
   * Unique identifier for the reflection.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * Title of the reflection.
   */
  @Column(nullable = false)
  private String title;

  /**
   * Content of the reflection.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /**
   * User who authored the reflection.
   */
  @ManyToOne(optional = false)
  @JoinColumn(name = "author_id")
  private User author;

  /**
   * Visibility level of the reflection (PUBLIC, HOUSEHOLD, or PRIVATE).
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VisibilityType visibility;

  /**
   * The household associated with the reflection. Only relevant when visibility is HOUSEHOLD.
   */
  @ManyToOne
  @JoinColumn(name = "household_id")
  private Household household;

  /**
   * The ID of the event this reflection is associated with. Optional.
   */
  @Column(name = "event_id")
  private Long eventId;

  /**
   * Date and time when the reflection was created.
   */
  @CreationTimestamp
  private LocalDateTime createdAt;

  /**
   * Date and time when the reflection was last updated.
   */
  @UpdateTimestamp
  private LocalDateTime updatedAt;
}