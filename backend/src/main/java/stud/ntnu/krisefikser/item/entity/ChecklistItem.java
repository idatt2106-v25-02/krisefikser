package stud.ntnu.krisefikser.item.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.enums.ChecklistType;

/**
 * Represents an item in a checklist.
 * <p>
 * Each checklist item is associated with a household and has a name, icon, checked status, and
 * type.
 * </p>
 */
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

  private ChecklistType type;

  public ChecklistItemResponse toResponse() {
    return ChecklistItemResponse.builder()
        .id(id)
        .name(name)
        .icon(icon)
        .checked(checked)
        .build();
  }
}
