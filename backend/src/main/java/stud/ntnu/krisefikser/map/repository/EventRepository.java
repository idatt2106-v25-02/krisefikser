package stud.ntnu.krisefikser.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}