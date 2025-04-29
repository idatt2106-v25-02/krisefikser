package stud.ntnu.krisefikser.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.user.dto.UserDto;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
