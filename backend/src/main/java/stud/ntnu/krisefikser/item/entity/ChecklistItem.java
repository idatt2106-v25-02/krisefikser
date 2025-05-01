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
 * Entity representing an item in an emergency preparedness checklist.
 * 
 * <p>Checklist items help households track important tasks or items they should verify
 * or have on hand during emergency situations. Each item can be marked as checked to
 * indicate completion or availability.</p>
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistItem {

  /**
   * Unique identifier for the checklist item.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The household this checklist item belongs to.
   */
  @ManyToOne
  @JoinColumn(name = "household_id", nullable = false)
  private Household household;

  /**
   * Name or description of the checklist item.
   */
  @Column(nullable = false)
  private String name;

  /**
   * Icon identifier representing the checklist item visually in the UI.
   */
  private String icon;

  /**
   * Whether the item has been checked (completed/verified) or not.
   */
  @Column(nullable = false)
  private Boolean checked = false;

  /**
   * Categorization of the checklist item.
   */
  private ChecklistType type;

  /**
   * Converts this entity to a response DTO.
   *
   * @return a DTO containing the essential information about this checklist item
   */
  public ChecklistItemResponse toResponse() {
    return ChecklistItemResponse.builder()
        .id(id)
        .name(name)
        .icon(icon)
        .checked(checked)
        .build();
  }
}
