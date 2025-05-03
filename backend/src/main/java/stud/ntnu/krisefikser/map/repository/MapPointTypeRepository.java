package stud.ntnu.krisefikser.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import java.util.Optional;

public interface MapPointTypeRepository extends JpaRepository<MapPointType, Long> {
    Optional<MapPointType> findByTitle(String title);
}