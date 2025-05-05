package stud.ntnu.krisefikser.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.MapPoint;

public interface MapPointRepository extends JpaRepository<MapPoint, Long> {

}