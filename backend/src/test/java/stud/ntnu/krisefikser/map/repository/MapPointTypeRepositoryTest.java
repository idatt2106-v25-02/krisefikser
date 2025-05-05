package stud.ntnu.krisefikser.map.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.map.entity.MapPointType;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class MapPointTypeRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private MapPointTypeRepository mapPointTypeRepository;

  @Test
  void save_ShouldPersistMapPointType() {
    // Create a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Hospital")
        .iconUrl("/images/icons/hospital.png")
        .description("Medical facility")
        .openingTime("24/7")
        .build();

    // Save the map point type
    MapPointType savedMapPointType = mapPointTypeRepository.save(mapPointType);

    // Flush changes to the database
    entityManager.flush();

    // Verify the map point type was saved with an ID
    assertThat(savedMapPointType.getId()).isNotNull();
    assertThat(savedMapPointType.getTitle()).isEqualTo("Hospital");
    assertThat(savedMapPointType.getIconUrl()).isEqualTo("/images/icons/hospital.png");
    assertThat(savedMapPointType.getDescription()).isEqualTo("Medical facility");
    assertThat(savedMapPointType.getOpeningTime()).isEqualTo("24/7");
  }

  @Test
  void findById_ShouldReturnMapPointType() {
    // Create and persist a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Police Station")
        .iconUrl("/images/icons/police.png")
        .description("Law enforcement facility")
        .openingTime("24/7")
        .build();
    MapPointType persistedMapPointType = entityManager.persist(mapPointType);
    entityManager.flush();

    // Find the map point type by ID
    Optional<MapPointType> foundMapPointType = mapPointTypeRepository.findById(
        persistedMapPointType.getId());

    // Verify map point type was found
    assertThat(foundMapPointType).isPresent();
    assertThat(foundMapPointType.get().getTitle()).isEqualTo("Police Station");
    assertThat(foundMapPointType.get().getIconUrl()).isEqualTo("/images/icons/police.png");
  }

  @Test
  void findAll_ShouldReturnAllMapPointTypes() {
    // Create and persist multiple map point types
    MapPointType mapPointType1 = MapPointType.builder()
        .title("Shelter")
        .iconUrl("/images/icons/shelter.png")
        .description("Emergency shelter")
        .openingTime("24/7")
        .build();

    MapPointType mapPointType2 = MapPointType.builder()
        .title("Food Distribution")
        .iconUrl("/images/icons/food.png")
        .description("Food distribution center")
        .openingTime("08:00-20:00")
        .build();

    entityManager.persist(mapPointType1);
    entityManager.persist(mapPointType2);
    entityManager.flush();

    // Find all map point types
    List<MapPointType> mapPointTypes = mapPointTypeRepository.findAll();

    // Verify map point types were found (note: there might be map point types from other tests)
    assertThat(mapPointTypes).hasSizeGreaterThanOrEqualTo(2);
    assertThat(mapPointTypes).extracting(MapPointType::getTitle)
        .contains("Shelter", "Food Distribution");
  }

  @Test
  void deleteById_ShouldRemoveMapPointType() {
    // Create and persist a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Water Supply")
        .iconUrl("/images/icons/water.png")
        .description("Water distribution point")
        .openingTime("24/7")
        .build();
    MapPointType persistedMapPointType = entityManager.persist(mapPointType);
    entityManager.flush();

    // Delete the map point type
    mapPointTypeRepository.deleteById(persistedMapPointType.getId());
    entityManager.flush();

    // Try to find the deleted map point type
    Optional<MapPointType> deletedMapPointType = mapPointTypeRepository.findById(
        persistedMapPointType.getId());

    // Verify map point type was deleted
    assertThat(deletedMapPointType).isNotPresent();
  }

  @Test
  void update_ShouldUpdateExistingMapPointType() {
    // Create and persist a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Pharmacy")
        .iconUrl("/images/icons/pharmacy.png")
        .description("Medical supplies")
        .openingTime("08:00-22:00")
        .build();
    MapPointType persistedMapPointType = entityManager.persist(mapPointType);
    entityManager.flush();

    // Update the map point type
    persistedMapPointType.setTitle("24H Pharmacy");
    persistedMapPointType.setOpeningTime("24/7");
    mapPointTypeRepository.save(persistedMapPointType);
    entityManager.flush();

    // Find the updated map point type
    Optional<MapPointType> updatedMapPointType = mapPointTypeRepository.findById(
        persistedMapPointType.getId());

    // Verify the map point type was updated
    assertThat(updatedMapPointType).isPresent();
    assertThat(updatedMapPointType.get().getTitle()).isEqualTo("24H Pharmacy");
    assertThat(updatedMapPointType.get().getOpeningTime()).isEqualTo("24/7");
  }
}