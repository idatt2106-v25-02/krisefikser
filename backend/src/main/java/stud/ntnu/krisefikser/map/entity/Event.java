package stud.ntnu.krisefikser.map.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.reflection.entity.Reflection;

/**
 * Entity class representing an event in the system. This class is used to store information about
 * events, including their title, description, location, level, start and end times, and status.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private Double radius;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventLevel level;

  @Column(nullable = false)
  private ZonedDateTime startTime;

  private ZonedDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status;

  @OneToMany(mappedBy = "event", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Set<Notification> notifications = new HashSet<>();

  @OneToMany(mappedBy = "event", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Set<Reflection> reflections = new HashSet<>();

  /**
   * Converts the Event entity to an EventResponse DTO. This method is used to transfer event data
   * to the client.
   *
   * @return EventResponse DTO containing event data
   */
  public EventResponse toResponse() {
    return EventResponse.builder()
        .id(id)
        .title(title)
        .description(description)
        .radius(radius)
        .latitude(latitude)
        .longitude(longitude)
        .level(level)
        .startTime(startTime)
        .endTime(endTime)
        .status(status)
        .build();
  }
}