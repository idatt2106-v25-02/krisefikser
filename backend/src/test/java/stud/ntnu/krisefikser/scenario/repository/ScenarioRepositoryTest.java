package stud.ntnu.krisefikser.scenario.repository;

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
import stud.ntnu.krisefikser.scenario.entity.Scenario;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class ScenarioRepositoryTest {

  @Autowired
  private ScenarioRepository scenarioRepository;

  private Scenario scenario1;

  @BeforeEach
  void setUp() {
    // Clear previous data
    scenarioRepository.deleteAll();

    // Create test scenarios
    scenario1 = Scenario.builder()
        .title("Earthquake Scenario")
        .content("A major earthquake in an urban area")
        .build();
    scenario1 = scenarioRepository.save(scenario1);

    Scenario scenario2 = Scenario.builder()
        .title("Flood Scenario")
        .content("Coastal flooding due to heavy rainfall")
        .build();
    scenario2 = scenarioRepository.save(scenario2);
  }

  @Test
  void save_ShouldPersistScenario() {
    // Arrange
    Scenario newScenario = Scenario.builder()
        .title("Fire Scenario")
        .content("Major forest fire event")
        .build();

    // Act
    Scenario savedScenario = scenarioRepository.save(newScenario);

    // Assert
    assertThat(savedScenario.getId()).isNotNull();
    assertThat(savedScenario.getTitle()).isEqualTo("Fire Scenario");
    assertThat(savedScenario.getContent()).isEqualTo("Major forest fire event");
  }

  @Test
  void findById_ShouldReturnScenario_WhenScenarioExists() {
    // Act
    Optional<Scenario> result = scenarioRepository.findById(scenario1.getId());

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getTitle()).isEqualTo("Earthquake Scenario");
    assertThat(result.get().getContent()).isEqualTo("A major earthquake in an urban area");
  }

  @Test
  void findById_ShouldReturnEmpty_WhenScenarioDoesNotExist() {
    // Act
    Optional<Scenario> result = scenarioRepository.findById(UUID.randomUUID());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findAll_ShouldReturnAllScenarios() {
    // Act
    List<Scenario> result = scenarioRepository.findAll();

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder("Earthquake Scenario", "Flood Scenario");
  }

  @Test
  void update_ShouldModifyExistingScenario() {
    // Arrange
    scenario1.setTitle("Updated Earthquake Scenario");
    scenario1.setContent("Updated description");

    // Act
    Scenario updatedScenario = scenarioRepository.save(scenario1);

    // Assert
    assertThat(updatedScenario.getTitle()).isEqualTo("Updated Earthquake Scenario");
    assertThat(updatedScenario.getContent()).isEqualTo("Updated description");

    // Verify in database
    Optional<Scenario> fromDb = scenarioRepository.findById(scenario1.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getTitle()).isEqualTo("Updated Earthquake Scenario");
  }

  @Test
  void delete_ShouldRemoveScenario() {
    // Act
    scenarioRepository.deleteById(scenario1.getId());

    // Assert
    assertThat(scenarioRepository.findById(scenario1.getId())).isEmpty();
    assertThat(scenarioRepository.findAll()).hasSize(1);
  }

  @Test
  void deleteAll_ShouldRemoveAllScenarios() {
    // Act
    scenarioRepository.deleteAll();

    // Assert
    assertThat(scenarioRepository.findAll()).isEmpty();
  }

  @Test
  void count_ShouldReturnNumberOfScenarios() {
    // Act
    long count = scenarioRepository.count();

    // Assert
    assertThat(count).isEqualTo(2);
  }
}