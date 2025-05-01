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

@Service
@RequiredArgsConstructor
public class ChecklistItemService {
    private final ChecklistItemRepository checklistItemRepository;

    @Transactional
    public ChecklistItemResponse toggleChecklistItem(UUID id) {
        return checklistItemRepository.findById(id)
                .map(item -> {
                    item.setChecked(!item.getChecked());
                    return checklistItemRepository.save(item).toResponse();
                })
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found with id: " + id));
    }

    public List<ChecklistItemResponse> getAllChecklistItems() {
        return checklistItemRepository.findAll().stream()
                .map(ChecklistItem::toResponse)
                .toList();
    }
}
