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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdInvite;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.reflection.entity.Reflection;
import stud.ntnu.krisefikser.user.dto.UserResponse;

/**
 * Entity class representing a user in the system. This class is used to store information about
 * users, including their email, roles, password, and preferences.
 *
 * @since 1.0
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@ToString(exclude = {"activeHousehold", "verificationTokens"})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
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
  private boolean locationSharing = true;

  @Column
  private Double latitude;

  @Column
  private Double longitude;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private int passwordRetries = 0;

  @Column
  private LocalDateTime lockedUntil;

  @ManyToOne
  @JoinColumn(name = "active_household_id")
  private Household activeHousehold;

  @Column(nullable = false)
  private boolean emailVerified = false;

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
  private Set<VerificationToken> verificationTokens = new HashSet<>();

  @OneToMany(mappedBy = "invitedUser", cascade = CascadeType.REMOVE)
  private Set<HouseholdInvite> householdInvites = new HashSet<>();

  @OneToMany(mappedBy = "createdBy", cascade = CascadeType.REMOVE)
  private Set<HouseholdInvite> sentHouseholdInvites = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
  private Set<HouseholdMember> householdMembers = new HashSet<>();

  @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
  private Set<Reflection> reflections = new HashSet<>();

  @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
  private Set<Household> ownerOfHouseholds = new HashSet<>();

  public boolean isSuperAdmin() {
    return roles.stream().anyMatch(role -> role.getName() == RoleType.SUPER_ADMIN);
  }

  /**
   * Converts the User entity to a UserResponse DTO without location data.
   *
   * @return a UserResponse DTO containing user information without location data.
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
        locationSharing,
        null, // Do not include latitude
        null); // Do not include longitude
  }

  /**
   * Converts the User entity to a UserResponse DTO including location data. This should only be
   * used in contexts where sharing location is appropriate, such as when getting the active
   * household.
   *
   * @return a UserResponse DTO containing user information including location
   * data.
   */
  public UserResponse toDtoWithLocation() {
    List<String> roleNames = roles.stream().map(role -> role.getName().toString()).toList();

    return new UserResponse(
        id,
        email,
        roleNames,
        firstName,
        lastName,
        notifications,
        emailUpdates,
        locationSharing,
        latitude,
        longitude);
  }
}