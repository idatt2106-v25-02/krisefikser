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

  public enum RoleType {
    USER,
    ADMIN,
    SUPER_ADMIN
  }
}
