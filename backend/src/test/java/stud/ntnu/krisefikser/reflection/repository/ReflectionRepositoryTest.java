package stud.ntnu.krisefikser.reflection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.reflection.entity.Reflection;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class ReflectionRepositoryTest {

  private final Long testEventId = 123L;
  @Autowired
  private ReflectionRepository reflectionRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private HouseholdRepository householdRepository;
  @Autowired
  private HouseholdMemberRepository householdMemberRepository;
  private User user1;
  private User user2;
  private User user3;
  private Household household1;
  private Household household2;

  @BeforeEach
  void setUp() {
    // Clean up repos
    reflectionRepository.deleteAll();
    householdMemberRepository.deleteAll();
    householdRepository.deleteAll();
    userRepository.deleteAll();

    // Create test users
    user1 = userRepository.save(User.builder()
        .email("user1@example.com")
        .password("password")
        .build());

    user2 = userRepository.save(User.builder()
        .email("user2@example.com")
        .password("password")
        .build());

    user3 = userRepository.save(User.builder()
        .email("user3@example.com")
        .password("password")
        .build());

    // Create households with owners
    household1 = householdRepository.save(Household.builder()
        .name("Household 1")
        .address("123 Test Street")
        .city("Test City")
        .postalCode("12345")
        .latitude(0.0)
        .longitude(0.0)
        .waterLiters(0.0)
        .owner(user1)
        .build());

    household2 = householdRepository.save(Household.builder()
        .name("Household 2")
        .address("456 Test Avenue")
        .city("Test City")
        .postalCode("67890")
        .latitude(0.0)
        .longitude(0.0)
        .waterLiters(0.0)
        .owner(user2)
        .build());

    // Add users to households
    householdMemberRepository.save(HouseholdMember.builder()
        .household(household1)
        .user(user1)
        .build());

    householdMemberRepository.save(HouseholdMember.builder()
        .household(household2)
        .user(user2)
        .build());

    // Create reflections with different visibilities
    reflectionRepository.save(Reflection.builder()
        .title("Public Reflection")
        .content("Public content")
        .author(user1)
        .visibility(VisibilityType.PUBLIC)
        .build());

    reflectionRepository.save(Reflection.builder()
        .title("Private Reflection")
        .content("Private content")
        .author(user1)
        .visibility(VisibilityType.PRIVATE)
        .build());

    reflectionRepository.save(Reflection.builder()
        .title("Household Reflection")
        .content("Household content")
        .author(user1)
        .household(household1)
        .visibility(VisibilityType.HOUSEHOLD)
        .build());

    reflectionRepository.save(Reflection.builder()
        .title("Event Reflection")
        .content("Event content")
        .author(user2)
        .eventId(testEventId)
        .visibility(VisibilityType.PUBLIC)
        .build());
  }

  @Test
  void findByAuthorId_ShouldReturnAuthorReflections() {
    // Act
    List<Reflection> result = reflectionRepository.findByAuthorId(user1.getId());

    // Assert
    assertThat(result).hasSize(3);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder(
            "Public Reflection",
            "Private Reflection",
            "Household Reflection"
        );
  }

  @Test
  void findByAuthorId_ShouldReturnEmptyList_WhenNoReflectionsExist() {
    // Act
    List<Reflection> result = reflectionRepository.findByAuthorId(user3.getId());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByVisibility_ShouldReturnPublicReflections() {
    // Act
    List<Reflection> result = reflectionRepository.findByVisibility(VisibilityType.PUBLIC);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder("Public Reflection", "Event Reflection");
  }

  @Test
  void findByVisibility_ShouldReturnPrivateReflections() {
    // Act
    List<Reflection> result = reflectionRepository.findByVisibility(VisibilityType.PRIVATE);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Private Reflection");
  }

  @Test
  void findByVisibility_ShouldReturnHouseholdReflections() {
    // Act
    List<Reflection> result = reflectionRepository.findByVisibility(VisibilityType.HOUSEHOLD);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Household Reflection");
  }

  @Test
  void findByHouseholdId_ShouldReturnHouseholdReflections() {
    // Act
    List<Reflection> result = reflectionRepository.findByHouseholdId(household1.getId());

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Household Reflection");
  }

  @Test
  void findByHouseholdId_ShouldReturnEmptyList_WhenNoReflectionsExist() {
    // Act
    List<Reflection> result = reflectionRepository.findByHouseholdId(household2.getId());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findReflectionsAccessibleToUser_ShouldReturnAllAccessibleReflections_ForUser1() {
    // Act
    List<Reflection> result = reflectionRepository.findReflectionsAccessibleToUser(user1.getId());

    // Assert
    assertThat(result).hasSize(4);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder(
            "Public Reflection",
            "Private Reflection",
            "Household Reflection",
            "Event Reflection"
        );
  }

  @Test
  void findReflectionsAccessibleToUser_ShouldReturnOnlyPublicAndOwnPrivateReflections_ForUser2() {
    // Act
    List<Reflection> result = reflectionRepository.findReflectionsAccessibleToUser(user2.getId());

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder("Public Reflection", "Event Reflection");
  }

  @Test
  void findReflectionsAccessibleToUser_ShouldReturnOnlyPublicReflections_ForUser3() {
    // Act
    List<Reflection> result = reflectionRepository.findReflectionsAccessibleToUser(user3.getId());

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder("Public Reflection", "Event Reflection");
  }

  @Test
  void findByEventId_ShouldReturnRelatedReflections() {
    // Act
    List<Reflection> result = reflectionRepository.findByEventId(testEventId);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Event Reflection");
  }

  @Test
  void findByEventId_ShouldReturnEmptyList_WhenNoReflectionsExist() {
    // Act
    List<Reflection> result = reflectionRepository.findByEventId(999L);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void standardCrudOperations_ShouldWorkCorrectly() {
    // Test save
    Reflection newReflection = Reflection.builder()
        .title("New Reflection")
        .content("New content")
        .author(user3)
        .visibility(VisibilityType.PUBLIC)
        .build();

    Reflection saved = reflectionRepository.save(newReflection);
    assertThat(saved.getId()).isNotNull();

    // Test findById
    Optional<Reflection> found = reflectionRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("New Reflection");

    // Test findById with non-existent ID
    Optional<Reflection> notFound = reflectionRepository.findById(UUID.randomUUID());
    assertThat(notFound).isEmpty();

    // Test update
    saved.setTitle("Updated Reflection");
    Reflection updated = reflectionRepository.save(saved);
    assertThat(updated.getTitle()).isEqualTo("Updated Reflection");

    // Test delete
    reflectionRepository.deleteById(saved.getId());
    assertThat(reflectionRepository.findById(saved.getId())).isEmpty();

    // Test findAll
    List<Reflection> allReflections = reflectionRepository.findAll();
    assertThat(allReflections).hasSize(4); // The original 4 reflections
  }
}