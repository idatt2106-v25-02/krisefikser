package stud.ntnu.krisefikser.household.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.repository.EventRepository;
import stud.ntnu.krisefikser.map.service.EventWebSocketService;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Service class that handles business logic related to household invitation entities.
 * Provides CRUD operations for invitations and integrates with
 * WebSocket notifications to broadcast changes to connected clients in real-time.
 *
 * <p>This service acts as an intermediary between the controllers and the data access layer,
 * adding business validation and integrating with the WebSocket service for real-time updates.
 * </p>
 *
 * @author NTNU Krisefikser Team
 * @see Event
 * @see EventRepository
 * @see EventWebSocketService
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "household_invite")
public class HouseholdInvite {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private Household household;

  @ManyToOne
  @JoinColumn(name = "invited_user_id")
  private User invitedUser;

  @Column(nullable = true)
  private String invitedEmail;

  @ManyToOne(optional = false)
  @JoinColumn(name = "created_by_id")
  private User createdBy;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private InviteStatus status;

  @Column(nullable = true)
  private LocalDateTime respondedAt;

  /**
   * An enum describing the status of an invitation.
   */
  public enum InviteStatus {
    PENDING, ACCEPTED, DECLINED, CANCELLED
  }
}