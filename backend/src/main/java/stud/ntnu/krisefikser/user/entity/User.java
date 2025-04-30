package stud.ntnu.krisefikser.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.dto.UserDto;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
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

  @ManyToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "active_household_id", nullable = true)
  private Household activeHousehold;

  public UserDto toDto() {
    List<String> roleNames = roles.stream().map(role -> role.getName().toString()).toList();

    return new UserDto(
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
