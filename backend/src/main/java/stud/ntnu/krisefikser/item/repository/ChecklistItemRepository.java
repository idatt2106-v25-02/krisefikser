package stud.ntnu.krisefikser.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;

import java.util.UUID;

/**
 * Repository interface for {@link ChecklistItem} entity operations.
 * 
 * <p>This repository provides basic CRUD operations for checklist items through Spring Data JPA.
 * It extends JpaRepository to enable standard database operations and uses UUID as the primary
 * key type for checklist items.</p>
 */
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {
}
