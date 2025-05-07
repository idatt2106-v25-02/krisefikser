package stud.ntnu.krisefikser.map.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.dto.MapPointResponse;

/**
 * Entity class representing a map point in the system. This class is used to store information
 * about map points, including their latitude, longitude, and type.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "map_points")
public class MapPoint {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "type_id", nullable = false)
  private MapPointType type;

  /**
   * Method for converting the entity to a response DTO.
   *
   * @return MapPointResponse object containing the map point information
   */
  public MapPointResponse toResponse() {
    return MapPointResponse.builder()
        .id(this.id)
        .latitude(this.latitude)
        .longitude(this.longitude)
        .type(this.type.toResponse())
        .build();
  }
}