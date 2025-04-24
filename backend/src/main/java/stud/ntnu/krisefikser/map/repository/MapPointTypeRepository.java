package stud.ntnu.krisefikser.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.MapPointType;

public interface MapPointTypeRepository extends JpaRepository<MapPointType, Long> {
}