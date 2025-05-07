package stud.ntnu.krisefikser.user.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.dto.UserResponse;

/**
 * Entity class representing a user in the system. This class is used to store
 * information about
 * users, including their email, roles, password, and preferences.
 *
 * @since 1.0
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@ToString(exclude = { "activeHousehold" })
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @Column(nullable = false)
  private String password;

  private String firstName;

  private String lastName;

  @Column(nullable = false)
  private boolean notifications = true;

  @Column(nullable = false)
  private boolean emailUpdates = true;

  @Column(nullable = false)
  private boolean locationSharing = false;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private int passwordRetries = 0;

  @Column
  private LocalDateTime lockedUntil;

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "active_household_id")
  private Household activeHousehold;

  @Column(nullable = false)
  private boolean emailVerified = false;

  public boolean isSuperAdmin() {
    return roles.stream().anyMatch(role -> role.getName() == RoleType.SUPER_ADMIN);
  }

  /**
   * Converts the User entity to a UserResponse DTO.
   *
   * @return a UserResponse DTO containing user information.
   */
  public UserResponse toDto() {
    List<String> roleNames = roles.stream().map(role -> role.getName().toString()).toList();

    return new UserResponse(
        id,
        email,
        roleNames,
        firstName,
        lastName,
        notifications,
        emailUpdates,
        locationSharing);
  }
}
