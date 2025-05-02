package stud.ntnu.krisefikser.map.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class EventRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private EventRepository eventRepository;

  @Test
  void save_ShouldPersistEvent() {
    // Create an event
    ZonedDateTime now = ZonedDateTime.now();
    Event event = Event.builder()
        .title("Flood Warning")
        .description("Area affected by rising water levels")
        .radius(1000.0)
        .latitude(63.4305)
        .longitude(10.3951)
        .level(EventLevel.YELLOW)
        .startTime(now)
        .endTime(now.plusHours(24))
        .status(EventStatus.ONGOING)
        .build();

    // Save the event
    Event savedEvent = eventRepository.save(event);

    // Flush changes to the database
    entityManager.flush();

    // Verify the event was saved with an ID
    assertThat(savedEvent.getId()).isNotNull();
    assertThat(savedEvent.getTitle()).isEqualTo("Flood Warning");
    assertThat(savedEvent.getLevel()).isEqualTo(EventLevel.YELLOW);
    assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.ONGOING);
  }

  @Test
  void findById_ShouldReturnEvent() {
    // Create and persist an event
    ZonedDateTime now = ZonedDateTime.now();
    Event event = Event.builder()
        .title("Winter Storm Alert")
        .description("Heavy snowfall expected")
        .radius(2000.0)
        .latitude(63.4200)
        .longitude(10.3900)
        .level(EventLevel.RED)
        .startTime(now)
        .endTime(now.plusHours(12))
        .status(EventStatus.ONGOING)
        .build();
    Event persistedEvent = entityManager.persist(event);
    entityManager.flush();

    // Find the event by ID
    Optional<Event> foundEvent = eventRepository.findById(persistedEvent.getId());

    // Verify event was found
    assertThat(foundEvent).isPresent();
    assertThat(foundEvent.get().getTitle()).isEqualTo("Winter Storm Alert");
    assertThat(foundEvent.get().getLevel()).isEqualTo(EventLevel.RED);
  }

  @Test
  void findAll_ShouldReturnAllEvents() {
    // Create and persist multiple events
    ZonedDateTime now = ZonedDateTime.now();
    Event event1 = Event.builder()
        .title("Power Outage")
        .description("Temporary power disruption")
        .radius(1500.0)
        .latitude(63.4100)
        .longitude(10.3800)
        .level(EventLevel.GREEN)
        .startTime(now)
        .endTime(now.plusHours(4))
        .status(EventStatus.ONGOING)
        .build();

    Event event2 = Event.builder()
        .title("Traffic Accident")
        .description("Major traffic disruption")
        .radius(500.0)
        .latitude(63.4250)
        .longitude(10.3950)
        .level(EventLevel.YELLOW)
        .startTime(now)
        .endTime(now.plusHours(2))
        .status(EventStatus.ONGOING)
        .build();

    entityManager.persist(event1);
    entityManager.persist(event2);
    entityManager.flush();

    // Find all events
    List<Event> events = eventRepository.findAll();

    // Verify events were found (note: there might be events from other tests)
    assertThat(events).hasSizeGreaterThanOrEqualTo(2);
    assertThat(events).extracting(Event::getTitle)
        .contains("Power Outage", "Traffic Accident");
  }

  @Test
  void deleteById_ShouldRemoveEvent() {
    // Create and persist an event
    ZonedDateTime now = ZonedDateTime.now();
    Event event = Event.builder()
        .title("Gas Leak")
        .description("Hazardous gas leak detected")
        .radius(800.0)
        .latitude(63.4300)
        .longitude(10.3920)
        .level(EventLevel.RED)
        .startTime(now)
        .endTime(now.plusHours(6))
        .status(EventStatus.ONGOING)
        .build();
    Event persistedEvent = entityManager.persist(event);
    entityManager.flush();

    // Delete the event
    eventRepository.deleteById(persistedEvent.getId());
    entityManager.flush();

    // Try to find the deleted event
    Optional<Event> deletedEvent = eventRepository.findById(persistedEvent.getId());

    // Verify event was deleted
    assertThat(deletedEvent).isNotPresent();
  }

  @Test
  void update_ShouldUpdateExistingEvent() {
    // Create and persist an event
    ZonedDateTime now = ZonedDateTime.now();
    Event event = Event.builder()
        .title("Water Main Break")
        .description("Water service disruption")
        .radius(1200.0)
        .latitude(63.4150)
        .longitude(10.3850)
        .level(EventLevel.YELLOW)
        .startTime(now)
        .endTime(now.plusHours(10))
        .status(EventStatus.ONGOING)
        .build();
    Event persistedEvent = entityManager.persist(event);
    entityManager.flush();

    // Update the event
    persistedEvent.setTitle("Major Water Main Break");
    persistedEvent.setLevel(EventLevel.RED);
    persistedEvent.setRadius(2000.0);
    persistedEvent.setEndTime(now.plusHours(24));
    eventRepository.save(persistedEvent);
    entityManager.flush();

    // Find the updated event
    Optional<Event> updatedEvent = eventRepository.findById(persistedEvent.getId());

    // Verify the event was updated
    assertThat(updatedEvent).isPresent();
    assertThat(updatedEvent.get().getTitle()).isEqualTo("Major Water Main Break");
    assertThat(updatedEvent.get().getLevel()).isEqualTo(EventLevel.RED);
    assertThat(updatedEvent.get().getRadius()).isEqualTo(2000.0);
  }

  @Test
  void status_ShouldBeUpdatable() {
    // Create and persist an event
    ZonedDateTime now = ZonedDateTime.now();
    Event event = Event.builder()
        .title("Upcoming Public Gathering")
        .description("Large public gathering scheduled")
        .radius(300.0)
        .latitude(63.4220)
        .longitude(10.3930)
        .level(EventLevel.GREEN)
        .startTime(now.plusDays(1))
        .endTime(now.plusDays(1).plusHours(5))
        .status(EventStatus.UPCOMING)
        .build();
    Event persistedEvent = entityManager.persist(event);
    entityManager.flush();

    // Update the event status
    persistedEvent.setStatus(EventStatus.ONGOING);
    eventRepository.save(persistedEvent);
    entityManager.flush();

    // Find the updated event
    Optional<Event> updatedEvent = eventRepository.findById(persistedEvent.getId());

    // Verify the event status was updated
    assertThat(updatedEvent).isPresent();
    assertThat(updatedEvent.get().getStatus()).isEqualTo(EventStatus.ONGOING);
  }
}