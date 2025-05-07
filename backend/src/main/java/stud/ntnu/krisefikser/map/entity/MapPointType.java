package stud.ntnu.krisefikser.map.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;

/**
 * Entity class representing a type of map point in the system. This class is used to store
 * information about different types of map points, including their title, icon URL, description,
 * and opening time.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "map_point_types")
public class MapPointType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String iconUrl;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String openingTime;

  @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<MapPoint> mapPoints;

  /**
   * Method for converting the entity to a response DTO.
   *
   * @return MapPointTypeResponse object containing the map point type information
   */
  public MapPointTypeResponse toResponse() {
    return MapPointTypeResponse.builder()
        .id(this.id)
        .title(this.title)
        .iconUrl(this.iconUrl)
        .description(this.description)
        .openingTime(this.openingTime)
        .build();
  }
}