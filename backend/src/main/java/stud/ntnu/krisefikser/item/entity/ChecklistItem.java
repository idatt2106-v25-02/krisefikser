package stud.ntnu.krisefikser.item.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Column(nullable = false)
    private Boolean checked = false;

    public ChecklistItemResponse toResponse() {
        return ChecklistItemResponse.builder()
                .id(id)
                .name(name)
                .icon(icon)
                .checked(checked)
                .build();
    }
}
