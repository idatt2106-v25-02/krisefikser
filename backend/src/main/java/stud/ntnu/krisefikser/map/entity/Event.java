package stud.ntnu.krisefikser.map.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.dto.EventResponse;

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

  public EventResponse toResponse() {
    return EventResponse.builder()
        .id(this.id)
        .title(this.title)
        .description(this.description)
        .radius(this.radius)
        .latitude(this.latitude)
        .longitude(this.longitude)
        .level(this.level)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .status(this.status)
        .build();
  }
}