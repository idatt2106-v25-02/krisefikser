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
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import stud.ntnu.krisefikser.household.dto.HouseholdMemberResponse;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Entity representing a member of a household.
 *
 * @since 1.0
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "household_id" }) })
@ToString(exclude = { "user", "household" })
public class HouseholdMember {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private Household household;

  public HouseholdMemberResponse toDto() {
    return new HouseholdMemberResponse(
        user.toDto());
  }
}
