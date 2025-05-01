package stud.ntnu.krisefikser.item.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistType;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ChecklistItemRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ChecklistItemRepository checklistItemRepository;

  @Test
  void save_ShouldPersistChecklistItem() {
    // Create a user and household
    User owner = User.builder()
        .email("checklist-user1@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Checklist Test Household 1")
        .owner(owner)
        .address("Test Address 1")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create a checklist item
    ChecklistItem item = ChecklistItem.builder()
        .name("First Aid Kit")
        .type(ChecklistType.HEALTH)
        .checked(false)
        .household(household)
        .build();

    // Save the item
    ChecklistItem savedItem = checklistItemRepository.save(item);

    // Flush changes to the database
    entityManager.flush();

    // Verify the item was saved with an ID
    assertThat(savedItem.getId()).isNotNull();
    assertThat(savedItem.getName()).isEqualTo("First Aid Kit");
    assertThat(savedItem.getType()).isEqualTo(ChecklistType.HEALTH);
    assertThat(savedItem.getChecked()).isFalse();
  }

  @Test
  void findById_ShouldReturnChecklistItem() {
    // Create a user and household
    User owner = User.builder()
        .email("checklist-user2@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Checklist Test Household 2")
        .owner(owner)
        .address("Test Address 2")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist a checklist item
    ChecklistItem item = ChecklistItem.builder()
        .name("Flashlight")
        .type(ChecklistType.POWER)
        .checked(true)
        .household(household)
        .build();

    ChecklistItem persistedItem = entityManager.persist(item);
    entityManager.flush();

    // Find the item by ID
    Optional<ChecklistItem> foundItem = checklistItemRepository.findById(persistedItem.getId());

    // Verify item was found
    assertThat(foundItem).isPresent();
    assertThat(foundItem.get().getName()).isEqualTo("Flashlight");
    assertThat(foundItem.get().getChecked()).isTrue();
  }

  @Test
  void findAll_ShouldReturnAllChecklistItems() {
    // Create a user and household
    User owner = User.builder()
        .email("checklist-user3@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Checklist Test Household 3")
        .owner(owner)
        .address("Test Address 3")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist multiple checklist items
    ChecklistItem item1 = ChecklistItem.builder()
        .name("Water Bottles")
        .type(ChecklistType.OTHER)
        .checked(false)
        .household(household)
        .build();

    ChecklistItem item2 = ChecklistItem.builder()
        .name("Contact Family")
        .type(ChecklistType.COMMUNICATION)
        .checked(true)
        .household(household)
        .build();

    entityManager.persist(item1);
    entityManager.persist(item2);
    entityManager.flush();

    // Find all checklist items
    List<ChecklistItem> items = checklistItemRepository.findAll();

    // Verify items were found (note: there might be items from other tests)
    assertThat(items).hasSizeGreaterThanOrEqualTo(2);
    assertThat(items).extracting(ChecklistItem::getName)
        .contains("Water Bottles", "Contact Family");
  }

  @Test
  void deleteById_ShouldRemoveChecklistItem() {
    // Create a user and household
    User owner = User.builder()
        .email("checklist-user4@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Checklist Test Household 4")
        .owner(owner)
        .address("Test Address 4")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist a checklist item
    ChecklistItem item = ChecklistItem.builder()
        .name("Batteries")
        .type(ChecklistType.POWER)
        .checked(false)
        .household(household)
        .build();

    ChecklistItem persistedItem = entityManager.persist(item);
    entityManager.flush();

    // Delete the item
    checklistItemRepository.deleteById(persistedItem.getId());
    entityManager.flush();

    // Try to find the deleted item
    Optional<ChecklistItem> deletedItem = checklistItemRepository.findById(persistedItem.getId());

    // Verify item was deleted
    assertThat(deletedItem).isNotPresent();
  }

  @Test
  void update_ShouldUpdateExistingChecklistItem() {
    // Create a user and household
    User owner = User.builder()
        .email("checklist-user5@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Checklist Test Household 5")
        .owner(owner)
        .address("Test Address 5")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist a checklist item
    ChecklistItem item = ChecklistItem.builder()
        .name("Emergency Plan")
        .type(ChecklistType.OTHER)
        .checked(false)
        .household(household)
        .build();

    ChecklistItem persistedItem = entityManager.persist(item);
    entityManager.flush();

    // Update the item
    persistedItem.setChecked(true);
    checklistItemRepository.save(persistedItem);
    entityManager.flush();

    // Find the updated item
    Optional<ChecklistItem> updatedItem = checklistItemRepository.findById(persistedItem.getId());

    // Verify the item was updated
    assertThat(updatedItem).isPresent();
    assertThat(updatedItem.get().getChecked()).isTrue();
  }
} 