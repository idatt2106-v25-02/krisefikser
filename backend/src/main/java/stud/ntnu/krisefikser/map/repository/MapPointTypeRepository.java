package stud.ntnu.krisefikser.map.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.MapPointType;

public interface MapPointTypeRepository extends JpaRepository<MapPointType, Long> {

  Optional<MapPointType> findByTitle(String title);
}