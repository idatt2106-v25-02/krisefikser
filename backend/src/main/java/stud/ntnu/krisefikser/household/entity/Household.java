package stud.ntnu.krisefikser.household.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.HashSet;
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
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Entity representing a household.
 *
 * @since 1.0
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"members"})
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

  @Column(nullable = false)
  @Builder.Default
  private Double waterLiters = 0.0;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<HouseholdMember> members = new HashSet<>();

  @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Guest> guests = new HashSet<>();
}
