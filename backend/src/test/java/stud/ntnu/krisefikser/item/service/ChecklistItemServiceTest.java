package stud.ntnu.krisefikser.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistType;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;

@ExtendWith(MockitoExtension.class)
class ChecklistItemServiceTest {

  @Mock
  private ChecklistItemRepository checklistItemRepository;

  @InjectMocks
  private ChecklistItemService checklistItemService;

  private UUID itemId;
  private ChecklistItem checklistItem;
  private ChecklistItem toggledItem;
  private Household household;

  @BeforeEach
  void setUp() {
    itemId = UUID.randomUUID();
    household = new Household();
    household.setId(UUID.randomUUID());
    household.setName("Test Household");

    checklistItem = ChecklistItem.builder()
        .id(itemId)
        .name("First Aid Kit")
        .icon("health_icon")
        .checked(false)
        .type(ChecklistType.HEALTH)
        .household(household)
        .build();

    toggledItem = ChecklistItem.builder()
        .id(itemId)
        .name("First Aid Kit")
        .icon("health_icon")
        .checked(true)
        .type(ChecklistType.HEALTH)
        .household(household)
        .build();
  }

  @Test
  void toggleChecklistItem_WhenUnchecked_ShouldToggleToChecked() {
    // Arrange
    when(checklistItemRepository.findById(itemId)).thenReturn(Optional.of(checklistItem));
    when(checklistItemRepository.save(any(ChecklistItem.class))).thenReturn(toggledItem);

    // Act
    ChecklistItemResponse result = checklistItemService.toggleChecklistItem(itemId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(itemId);
    assertThat(result.getName()).isEqualTo("First Aid Kit");
    assertThat(result.getIcon()).isEqualTo("health_icon");
    assertThat(result.getChecked()).isTrue();

    verify(checklistItemRepository).findById(itemId);
    verify(checklistItemRepository).save(any(ChecklistItem.class));
  }

  @Test
  void toggleChecklistItem_WhenChecked_ShouldToggleToUnchecked() {
    // Arrange
    ChecklistItem checkedItem = ChecklistItem.builder()
        .id(itemId)
        .name("First Aid Kit")
        .icon("health_icon")
        .checked(true)
        .type(ChecklistType.HEALTH)
        .household(household)
        .build();

    ChecklistItem uncheckedItem = ChecklistItem.builder()
        .id(itemId)
        .name("First Aid Kit")
        .icon("health_icon")
        .checked(false)
        .type(ChecklistType.HEALTH)
        .household(household)
        .build();

    when(checklistItemRepository.findById(itemId)).thenReturn(Optional.of(checkedItem));
    when(checklistItemRepository.save(any(ChecklistItem.class))).thenReturn(uncheckedItem);

    // Act
    ChecklistItemResponse result = checklistItemService.toggleChecklistItem(itemId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(itemId);
    assertThat(result.getName()).isEqualTo("First Aid Kit");
    assertThat(result.getIcon()).isEqualTo("health_icon");
    assertThat(result.getChecked()).isFalse();

    verify(checklistItemRepository).findById(itemId);
    verify(checklistItemRepository).save(any(ChecklistItem.class));
  }

  @Test
  void toggleChecklistItem_WithNonExistentId_ShouldThrowException() {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();
    when(checklistItemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> checklistItemService.toggleChecklistItem(nonExistentId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Checklist item not found with id: " + nonExistentId);

    verify(checklistItemRepository).findById(nonExistentId);
  }

  @Test
  void getAllChecklistItems_ShouldReturnAllItems() {
    // Arrange
    List<ChecklistItem> items = List.of(checklistItem);
    when(checklistItemRepository.findAll()).thenReturn(items);

    // Act
    List<ChecklistItemResponse> result = checklistItemService.getAllChecklistItems();

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(itemId);
    assertThat(result.getFirst().getName()).isEqualTo("First Aid Kit");
    assertThat(result.getFirst().getIcon()).isEqualTo("health_icon");
    assertThat(result.getFirst().getChecked()).isFalse();

    verify(checklistItemRepository).findAll();
  }

  @Test
  void getAllChecklistItems_WhenNoItems_ShouldReturnEmptyList() {
    // Arrange
    when(checklistItemRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<ChecklistItemResponse> result = checklistItemService.getAllChecklistItems();

    // Assert
    assertThat(result).isNotNull().isEmpty();

    verify(checklistItemRepository).findAll();
  }
} 