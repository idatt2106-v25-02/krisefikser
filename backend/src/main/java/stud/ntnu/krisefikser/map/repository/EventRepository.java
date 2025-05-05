package stud.ntnu.krisefikser.map.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

  @Query(
      "SELECT e FROM Event e WHERE (e.status = :ongoingStatus OR e.status = :upcomingStatus) AND e.endTime >= :currentTime")
  List<Event> findActiveEvents(
      @Param("ongoingStatus") EventStatus ongoingStatus,
      @Param("upcomingStatus") EventStatus upcomingStatus,
      @Param("currentTime") LocalDateTime currentTime);
}