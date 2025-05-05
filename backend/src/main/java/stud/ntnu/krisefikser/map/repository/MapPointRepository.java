package stud.ntnu.krisefikser.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.MapPoint;

/**
 * Repository interface for managing {@link MapPoint} entities. This interface extends JpaRepository
 * to provide CRUD operations and custom query methods.
 */
public interface MapPointRepository extends JpaRepository<MapPoint, Long> {

}