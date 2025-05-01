package stud.ntnu.krisefikser.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for managing checklist items in the emergency preparedness system.
 * 
 * <p>This service provides functionality for retrieving and toggling the completion status
 * of checklist items. It interacts with the {@link ChecklistItemRepository} for data access
 * operations.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistItemService {
    /**
     * Repository for ChecklistItem entity operations.
     */
    private final ChecklistItemRepository checklistItemRepository;

    /**
     * Toggles the checked status of a checklist item.
     * 
     * <p>This method finds the checklist item by ID and inverts its current checked status.
     * If the item is currently checked, it will be unchecked, and vice versa.</p>
     *
     * @param id the unique identifier of the checklist item to toggle
     * @return a response DTO containing the updated checklist item details
     * @throws EntityNotFoundException if no checklist item exists with the given ID
     */
    @Transactional
    public ChecklistItemResponse toggleChecklistItem(UUID id) {
        return checklistItemRepository.findById(id)
                .map(item -> {
                    item.setChecked(!item.getChecked());
                    return checklistItemRepository.save(item).toResponse();
                })
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found with id: " + id));
    }

    /**
     * Retrieves all checklist items in the system.
     * 
     * <p>This method fetches all checklist items from the repository and converts them
     * to response DTOs.</p>
     *
     * @return a list of checklist item response DTOs
     */
    public List<ChecklistItemResponse> getAllChecklistItems() {
        return checklistItemRepository.findAll().stream()
                .map(ChecklistItem::toResponse)
                .toList();
    }
}
