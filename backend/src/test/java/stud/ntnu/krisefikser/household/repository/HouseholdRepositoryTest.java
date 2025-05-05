package stud.ntnu.krisefikser.household.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class HouseholdRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private HouseholdRepository householdRepository;

  @Test
  void save_ShouldPersistHousehold() {
    // Create a user
    User owner = User.builder()
        .email("test-owner@example.com")
        .firstName("Test")
        .lastName("Owner")
        .password("password")
        .build();
    entityManager.persist(owner);

    // Create a household
    Household household = Household.builder()
        .name("Test Household")
        .owner(owner)
        .address("Test Address")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    // Save the household
    Household savedHousehold = householdRepository.save(household);

    // Flush changes to the database
    entityManager.flush();

    // Verify the household was saved with an ID
    assertThat(savedHousehold.getId()).isNotNull();
    assertThat(savedHousehold.getName()).isEqualTo("Test Household");
    assertThat(savedHousehold.getOwner()).isEqualTo(owner);
    assertThat(savedHousehold.getAddress()).isEqualTo("Test Address");
    assertThat(savedHousehold.getWaterLiters()).isEqualTo(100.0);
  }

  @Test
  void findById_ShouldReturnHousehold() {
    // Create and persist a user
    User owner = User.builder()
        .email("find-by-id@example.com")
        .firstName("Find")
        .lastName("ById")
        .password("password")
        .build();
    entityManager.persist(owner);

    // Create and persist a household
    Household household = Household.builder()
        .name("Find Me Household")
        .owner(owner)
        .address("Find Address")
        .city("Find City")
        .postalCode("54321")
        .latitude(63.4100)
        .longitude(10.3800)
        .waterLiters(200.0)
        .build();
    Household persistedHousehold = entityManager.persist(household);
    entityManager.flush();

    // Find the household by ID
    Optional<Household> foundHousehold = householdRepository.findById(persistedHousehold.getId());

    // Verify household was found
    assertThat(foundHousehold).isPresent();
    assertThat(foundHousehold.get().getName()).isEqualTo("Find Me Household");
    assertThat(foundHousehold.get().getOwner()).isEqualTo(owner);
  }

  @Test
  void findAll_ShouldReturnAllHouseholds() {
    // Create and persist a user
    User owner = User.builder()
        .email("find-all@example.com")
        .firstName("Find")
        .lastName("All")
        .password("password")
        .build();
    entityManager.persist(owner);

    // Create and persist multiple households
    Household household1 = Household.builder()
        .name("Household One")
        .owner(owner)
        .address("Address One")
        .city("City One")
        .postalCode("11111")
        .latitude(63.4200)
        .longitude(10.3900)
        .waterLiters(150.0)
        .build();

    Household household2 = Household.builder()
        .name("Household Two")
        .owner(owner)
        .address("Address Two")
        .city("City Two")
        .postalCode("22222")
        .latitude(63.4300)
        .longitude(10.4000)
        .waterLiters(250.0)
        .build();

    entityManager.persist(household1);
    entityManager.persist(household2);
    entityManager.flush();

    // Find all households
    List<Household> households = householdRepository.findAll();

    // Verify households were found (note: there might be households from other tests)
    assertThat(households).hasSizeGreaterThanOrEqualTo(2);
    assertThat(households).extracting(Household::getName)
        .contains("Household One", "Household Two");
  }

  @Test
  void deleteById_ShouldRemoveHousehold() {
    // Create and persist a user
    User owner = User.builder()
        .email("delete-test@example.com")
        .firstName("Delete")
        .lastName("Test")
        .password("password")
        .build();
    entityManager.persist(owner);

    // Create and persist a household
    Household household = Household.builder()
        .name("Delete Me Household")
        .owner(owner)
        .address("Delete Address")
        .city("Delete City")
        .postalCode("33333")
        .latitude(63.4400)
        .longitude(10.4100)
        .waterLiters(300.0)
        .build();
    Household persistedHousehold = entityManager.persist(household);
    entityManager.flush();

    // Delete the household
    householdRepository.deleteById(persistedHousehold.getId());
    entityManager.flush();

    // Try to find the deleted household
    Optional<Household> deletedHousehold = householdRepository.findById(persistedHousehold.getId());

    // Verify household was deleted
    assertThat(deletedHousehold).isNotPresent();
  }

  @Test
  void update_ShouldUpdateExistingHousehold() {
    // Create and persist a user
    User owner = User.builder()
        .email("update-test@example.com")
        .firstName("Update")
        .lastName("Test")
        .password("password")
        .build();
    entityManager.persist(owner);

    // Create and persist a household
    Household household = Household.builder()
        .name("Original Name")
        .owner(owner)
        .address("Original Address")
        .city("Original City")
        .postalCode("44444")
        .latitude(63.4500)
        .longitude(10.4200)
        .waterLiters(400.0)
        .build();
    Household persistedHousehold = entityManager.persist(household);
    entityManager.flush();

    // Update the household
    persistedHousehold.setName("Updated Name");
    persistedHousehold.setAddress("Updated Address");
    persistedHousehold.setWaterLiters(500.0);
    householdRepository.save(persistedHousehold);
    entityManager.flush();

    // Find the updated household
    Optional<Household> updatedHousehold = householdRepository.findById(persistedHousehold.getId());

    // Verify the household was updated
    assertThat(updatedHousehold).isPresent();
    assertThat(updatedHousehold.get().getName()).isEqualTo("Updated Name");
    assertThat(updatedHousehold.get().getAddress()).isEqualTo("Updated Address");
    assertThat(updatedHousehold.get().getWaterLiters()).isEqualTo(500.0);
  }
}