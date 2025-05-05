package stud.ntnu.krisefikser.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistCategory;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ChecklistItemServiceTest {

  @Mock
  private ChecklistItemRepository checklistItemRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private ChecklistItemService checklistItemService;

  private UUID itemId;
  private ChecklistItem checklistItem;
  private ChecklistItem toggledItem;
  private Household household;
  private User user;

  @BeforeEach
  void setUp() {
    itemId = UUID.randomUUID();
    household = new Household();
    household.setId(UUID.randomUUID());
    household.setName("Test Household");

    user = TestDataFactory.createTestUser("test@example.com");
    user.setActiveHousehold(household);

    checklistItem = TestDataFactory.createTestChecklistItem(
        "First Aid Kit",
        ChecklistCategory.HEALTH_HYGIENE,
        false,
        household
    );
    checklistItem.setId(itemId);

    toggledItem = TestDataFactory.createTestChecklistItem(
        "First Aid Kit",
        ChecklistCategory.HEALTH_HYGIENE,
        true,
        household
    );
    toggledItem.setId(itemId);
    
    // Using lenient() to avoid UnnecessaryStubbingException for tests that don't use this mock
    lenient().when(userService.getCurrentUser()).thenReturn(user);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void toggleChecklistItem_ShouldToggleStatus(boolean initialCheckedState) {
    // Arrange
    ChecklistItem testItem = TestDataFactory.createTestChecklistItem(
        "First Aid Kit",
        ChecklistCategory.HEALTH_HYGIENE,
        initialCheckedState,
        household
    );
    testItem.setId(itemId);

    ChecklistItem expectedItem = TestDataFactory.createTestChecklistItem(
        "First Aid Kit",
        ChecklistCategory.HEALTH_HYGIENE,
        !initialCheckedState,
        household
    );
    expectedItem.setId(itemId);

    when(checklistItemRepository.findById(itemId)).thenReturn(Optional.of(testItem));
    when(checklistItemRepository.save(any(ChecklistItem.class))).thenReturn(expectedItem);

    // Act
    ChecklistItemResponse result = checklistItemService.toggleChecklistItem(itemId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(itemId);
    assertThat(result.getName()).isEqualTo("First Aid Kit");
    assertThat(result.getIcon()).isEqualTo("health_hygiene_icon");
    assertThat(result.getChecked()).isEqualTo(!initialCheckedState);

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
  void toggleChecklistItem_WithDifferentHouseholdId_ShouldThrowException() {
    // Arrange
    Household differentHousehold = new Household();
    differentHousehold.setId(UUID.randomUUID());
    
    ChecklistItem itemFromDifferentHousehold = TestDataFactory.createTestChecklistItem(
        "First Aid Kit",
        ChecklistCategory.HEALTH_HYGIENE,
        false,
        differentHousehold
    );
    itemFromDifferentHousehold.setId(itemId);
    
    when(checklistItemRepository.findById(itemId)).thenReturn(Optional.of(itemFromDifferentHousehold));

    // Act & Assert
    assertThatThrownBy(() -> checklistItemService.toggleChecklistItem(itemId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Checklist item not found with id: " + itemId);

    verify(checklistItemRepository).findById(itemId);
  }

  @Test
  void getAllChecklistItems_ShouldReturnAllItems() {
    // Arrange
    List<ChecklistItem> items = List.of(checklistItem);
    when(checklistItemRepository.findByHousehold(household)).thenReturn(items);

    // Act
    List<ChecklistItemResponse> result = checklistItemService.getAllChecklistItems();

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(itemId);
    assertThat(result.getFirst().getName()).isEqualTo("First Aid Kit");
    assertThat(result.getFirst().getIcon()).isEqualTo("health_hygiene_icon");
    assertThat(result.getFirst().getChecked()).isFalse();

    verify(checklistItemRepository).findByHousehold(household);
  }

  @Test
  void getAllChecklistItems_WhenNoItems_ShouldReturnEmptyList() {
    // Arrange
    when(checklistItemRepository.findByHousehold(household)).thenReturn(Collections.emptyList());

    // Act
    List<ChecklistItemResponse> result = checklistItemService.getAllChecklistItems();

    // Assert
    assertThat(result).isNotNull().isEmpty();

    verify(checklistItemRepository).findByHousehold(household);
  }
  
  @Test
  void createDefaultChecklistItems_ShouldCreateAllDefaultItems() {
    // Arrange
    Household newHousehold = new Household();
    newHousehold.setId(UUID.randomUUID());
    
    // Act
    checklistItemService.createDefaultChecklistItems(newHousehold);
    
    // Assert
    verify(checklistItemRepository).saveAll(any(List.class));
  }
} 