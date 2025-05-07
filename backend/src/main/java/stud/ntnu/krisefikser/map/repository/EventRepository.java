package stud.ntnu.krisefikser.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.map.entity.Event;

/**
 * Repository interface for managing {@link Event} entities. This interface extends JpaRepository to
 * provide CRUD operations and custom query methods.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

}