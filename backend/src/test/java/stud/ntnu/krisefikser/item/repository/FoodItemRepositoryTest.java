package stud.ntnu.krisefikser.item.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FoodItemRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private FoodItemRepository foodItemRepository;

  @Test
  void save_ShouldPersistFoodItem() {
    // Create a user and household
    User owner = User.builder()
        .email("test-user@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Test Household")
        .owner(owner)
        .address("Test Address 1")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create a food item
    FoodItem foodItem = FoodItem.builder()
        .name("Rice")
        .icon("rice_icon")
        .kcal(100)
        .expirationDate(Instant.now().plusSeconds(86400 * 30)) // 30 days
        .household(household)
        .build();

    // Save the food item
    FoodItem savedItem = foodItemRepository.save(foodItem);

    // Flush changes to the database
    entityManager.flush();

    // Verify item was saved with an ID
    assertThat(savedItem.getId()).isNotNull();
    assertThat(savedItem.getName()).isEqualTo("Rice");
  }

  @Test
  void findById_ShouldReturnFoodItem() {
    // Create a user and household
    User owner = User.builder()
        .email("test-user2@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Test Household 2")
        .owner(owner)
        .address("Test Address 2")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist a food item
    FoodItem foodItem = FoodItem.builder()
        .name("Beans")
        .icon("beans_icon")
        .kcal(120)
        .expirationDate(Instant.now().plusSeconds(86400 * 60)) // 60 days
        .household(household)
        .build();

    FoodItem persistedItem = entityManager.persist(foodItem);
    entityManager.flush();

    // Find the food item by ID
    Optional<FoodItem> foundItem = foodItemRepository.findById(persistedItem.getId());

    // Verify item was found
    assertThat(foundItem).isPresent();
    assertThat(foundItem.get().getName()).isEqualTo("Beans");
  }

  @Test
  void findByHousehold_ShouldReturnAllFoodItemsForHousehold() {
    // Create a user and household
    User owner = User.builder()
        .email("test-user3@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Test Household 3")
        .owner(owner)
        .address("Test Address 3")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist multiple food items for the same household
    FoodItem item1 = FoodItem.builder()
        .name("Water")
        .icon("water_icon")
        .kcal(0)
        .expirationDate(Instant.now().plusSeconds(86400 * 365)) // 1 year
        .household(household)
        .build();

    FoodItem item2 = FoodItem.builder()
        .name("Pasta")
        .icon("pasta_icon")
        .kcal(350)
        .expirationDate(Instant.now().plusSeconds(86400 * 180)) // 180 days
        .household(household)
        .build();

    entityManager.persist(item1);
    entityManager.persist(item2);
    entityManager.flush();

    // Find all food items for the household
    List<FoodItem> foodItems = foodItemRepository.findByHousehold(household);

    // Verify items were found
    assertThat(foodItems).hasSize(2);
    assertThat(foodItems).extracting(FoodItem::getName).containsExactlyInAnyOrder("Water", "Pasta");
  }

  @Test
  void findByHousehold_WithDifferentHousehold_ShouldReturnEmpty() {
    // Create a user and two households
    User owner = User.builder()
        .email("test-user4@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household1 = Household.builder()
        .name("Test Household 4")
        .owner(owner)
        .address("Test Address 4")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    Household household2 = Household.builder()
        .name("Test Household 5")
        .owner(owner)
        .address("Test Address 5")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household1);
    entityManager.persist(household2);

    // Create and persist a food item for household1
    FoodItem foodItem = FoodItem.builder()
        .name("Canned Food")
        .icon("can_icon")
        .kcal(200)
        .expirationDate(Instant.now().plusSeconds(86400 * 365 * 2)) // 2 years
        .household(household1)
        .build();

    entityManager.persist(foodItem);
    entityManager.flush();

    // Try to find food items for household2
    List<FoodItem> foodItems = foodItemRepository.findByHousehold(household2);

    // Verify no items were found
    assertThat(foodItems).isEmpty();
  }

  @Test
  void deleteById_ShouldRemoveFoodItem() {
    // Create a user and household
    User owner = User.builder()
        .email("test-user5@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(owner);

    Household household = Household.builder()
        .name("Test Household 6")
        .owner(owner)
        .address("Test Address 6")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    entityManager.persist(household);

    // Create and persist a food item
    FoodItem foodItem = FoodItem.builder()
        .name("Cookies")
        .icon("cookie_icon")
        .kcal(450)
        .expirationDate(Instant.now().plusSeconds(86400 * 90)) // 90 days
        .household(household)
        .build();

    FoodItem persistedItem = entityManager.persist(foodItem);
    entityManager.flush();

    UUID itemId = persistedItem.getId();

    // Delete the food item
    foodItemRepository.deleteById(itemId);
    entityManager.flush();

    // Try to find the deleted item
    Optional<FoodItem> deletedItem = foodItemRepository.findById(itemId);

    // Verify item was deleted
    assertThat(deletedItem).isNotPresent();
  }
} 