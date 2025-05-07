package stud.ntnu.krisefikser.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity class representing a role in the system. This class is used to define the different
 */
@Entity
@Data
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id = 0L;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private RoleType name;

  /**
   * Enum representing the different types of roles in the system. This enum is used to define the
   * various roles that can be assigned to users.
   */
  public enum RoleType {
    USER,
    ADMIN,
    SUPER_ADMIN
  }
}
