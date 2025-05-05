package stud.ntnu.krisefikser.item.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;

/**
 * Repository interface for {@link ChecklistItem} entity operations.
 *
 * <p>This repository provides basic CRUD operations for checklist items through Spring Data JPA.
 * It extends JpaRepository to enable standard database operations and uses UUID as the primary key
 * type for checklist items.</p>
 */
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {

  /**
   * Finds all checklist items associated with a specific household.
   *
   * @param activeHousehold the household to find checklist items for
   * @return a list of checklist items associated with the specified household
   */
  List<ChecklistItem> findByHousehold(Household activeHousehold);
}
