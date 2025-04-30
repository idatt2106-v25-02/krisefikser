package stud.ntnu.krisefikser.item.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "household_id")
    private Household household;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Column(nullable = false)
    private Integer kcal;

    private Instant expirationDate;

    public FoodItemResponse toResponse() {
        return FoodItemResponse.builder()
                .id(id)
                .name(name)
                .icon(icon)
                .kcal(kcal)
                .expirationDate(expirationDate)
                .build();
    }
}
