package stud.ntnu.krisefikser.household.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import stud.ntnu.krisefikser.user.entity.User;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Household {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String postalCode;

  @Column(nullable = false)
  private String city;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id")
  private User owner;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<HouseholdMember> members = new HashSet<>();
}
