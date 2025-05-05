package stud.ntnu.krisefikser.map.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventStatus;

/**
 * Repository interface for managing {@link Event} entities. This interface extends JpaRepository to
 * provide CRUD operations and custom query methods.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

  /**
   * Finds all active events based on their status and end time.
   *
   * @param ongoingStatus  the status representing ongoing events
   * @param upcomingStatus the status representing upcoming events
   * @param currentTime    the current date and time
   * @return a list of active events
   */
  @Query(
      "SELECT e FROM Event e WHERE (e.status = :ongoingStatus OR e.status = :upcomingStatus)"
          + " AND e.endTime >= :currentTime")
  List<Event> findActiveEvents(
      @Param("ongoingStatus") EventStatus ongoingStatus,
      @Param("upcomingStatus") EventStatus upcomingStatus,
      @Param("currentTime") LocalDateTime currentTime);
}