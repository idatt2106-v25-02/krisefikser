package stud.ntnu.krisefikser.map.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.MapPointType;

/**
 * Repository interface for managing {@link MapPointType} entities. This interface extends
 * JpaRepository to provide CRUD operations and custom query methods.
 *
 * @since 1.0
 */
public interface MapPointTypeRepository extends JpaRepository<MapPointType, Long> {

  /**
   * Finds a MapPointType by its title.
   *
   * @param title the title of the MapPointType
   * @return an Optional containing the MapPointType if found, or empty if not found
   */
  Optional<MapPointType> findByTitle(String title);
}