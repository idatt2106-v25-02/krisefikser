package stud.ntnu.krisefikser.household.entity;

import jakarta.persistence.*;
import lombok.*;
import stud.ntnu.krisefikser.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public enum InviteStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    }
}