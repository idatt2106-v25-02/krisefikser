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
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class MapPointRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private MapPointRepository mapPointRepository;

  @Test
  void save_ShouldPersistMapPoint() {
    // Create a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Hospital")
        .iconUrl("/images/icons/hospital.png")
        .description("Medical facility")
        .openingTime("24/7")
        .build();
    entityManager.persist(mapPointType);

    // Create a map point
    MapPoint mapPoint = MapPoint.builder()
        .latitude(63.4305)
        .longitude(10.3951)
        .type(mapPointType)
        .build();

    // Save the map point
    MapPoint savedMapPoint = mapPointRepository.save(mapPoint);

    // Flush changes to the database
    entityManager.flush();

    // Verify the map point was saved with an ID
    assertThat(savedMapPoint.getId()).isNotNull();
    assertThat(savedMapPoint.getLatitude()).isEqualTo(63.4305);
    assertThat(savedMapPoint.getLongitude()).isEqualTo(10.3951);
    assertThat(savedMapPoint.getType()).isEqualTo(mapPointType);
  }

  @Test
  void findById_ShouldReturnMapPoint() {
    // Create a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Police Station")
        .iconUrl("/images/icons/police.png")
        .description("Law enforcement facility")
        .openingTime("24/7")
        .build();
    entityManager.persist(mapPointType);

    // Create and persist a map point
    MapPoint mapPoint = MapPoint.builder()
        .latitude(63.4215)
        .longitude(10.3850)
        .type(mapPointType)
        .build();
    MapPoint persistedMapPoint = entityManager.persist(mapPoint);
    entityManager.flush();

    // Find the map point by ID
    Optional<MapPoint> foundMapPoint = mapPointRepository.findById(persistedMapPoint.getId());

    // Verify map point was found
    assertThat(foundMapPoint).isPresent();
    assertThat(foundMapPoint.get().getLatitude()).isEqualTo(63.4215);
    assertThat(foundMapPoint.get().getLongitude()).isEqualTo(10.3850);
    assertThat(foundMapPoint.get().getType().getTitle()).isEqualTo("Police Station");
  }

  @Test
  void findAll_ShouldReturnAllMapPoints() {
    // Create a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Shelter")
        .iconUrl("/images/icons/shelter.png")
        .description("Emergency shelter")
        .openingTime("24/7")
        .build();
    entityManager.persist(mapPointType);

    // Create and persist multiple map points
    MapPoint mapPoint1 = MapPoint.builder()
        .latitude(63.4100)
        .longitude(10.3700)
        .type(mapPointType)
        .build();

    MapPoint mapPoint2 = MapPoint.builder()
        .latitude(63.4200)
        .longitude(10.3800)
        .type(mapPointType)
        .build();

    entityManager.persist(mapPoint1);
    entityManager.persist(mapPoint2);
    entityManager.flush();

    // Find all map points
    List<MapPoint> mapPoints = mapPointRepository.findAll();

    // Verify map points were found (note: there might be map points from other tests)
    assertThat(mapPoints).hasSizeGreaterThanOrEqualTo(2);
    assertThat(mapPoints).extracting(MapPoint::getLatitude)
        .contains(63.4100, 63.4200);
  }

  @Test
  void deleteById_ShouldRemoveMapPoint() {
    // Create a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Food Distribution")
        .iconUrl("/images/icons/food.png")
        .description("Food distribution center")
        .openingTime("08:00-20:00")
        .build();
    entityManager.persist(mapPointType);

    // Create and persist a map point
    MapPoint mapPoint = MapPoint.builder()
        .latitude(63.4150)
        .longitude(10.3750)
        .type(mapPointType)
        .build();
    MapPoint persistedMapPoint = entityManager.persist(mapPoint);
    entityManager.flush();

    // Delete the map point
    mapPointRepository.deleteById(persistedMapPoint.getId());
    entityManager.flush();

    // Try to find the deleted map point
    Optional<MapPoint> deletedMapPoint = mapPointRepository.findById(persistedMapPoint.getId());

    // Verify map point was deleted
    assertThat(deletedMapPoint).isNotPresent();
  }

  @Test
  void update_ShouldUpdateExistingMapPoint() {
    // Create a map point type
    MapPointType mapPointType = MapPointType.builder()
        .title("Water Supply")
        .iconUrl("/images/icons/water.png")
        .description("Water distribution point")
        .openingTime("24/7")
        .build();
    entityManager.persist(mapPointType);

    // Create and persist a map point
    MapPoint mapPoint = MapPoint.builder()
        .latitude(63.4250)
        .longitude(10.3850)
        .type(mapPointType)
        .build();
    MapPoint persistedMapPoint = entityManager.persist(mapPoint);
    entityManager.flush();

    // Update the map point
    persistedMapPoint.setLatitude(63.4260);
    persistedMapPoint.setLongitude(10.3860);
    mapPointRepository.save(persistedMapPoint);
    entityManager.flush();

    // Find the updated map point
    Optional<MapPoint> updatedMapPoint = mapPointRepository.findById(persistedMapPoint.getId());

    // Verify the map point was updated
    assertThat(updatedMapPoint).isPresent();
    assertThat(updatedMapPoint.get().getLatitude()).isEqualTo(63.4260);
    assertThat(updatedMapPoint.get().getLongitude()).isEqualTo(10.3860);
  }
}