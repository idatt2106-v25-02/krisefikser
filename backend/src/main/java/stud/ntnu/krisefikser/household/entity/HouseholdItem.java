package stud.ntnu.krisefikser.household.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = true)
    private LocalDateTime expiryDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "household_id")
    private Household household;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}