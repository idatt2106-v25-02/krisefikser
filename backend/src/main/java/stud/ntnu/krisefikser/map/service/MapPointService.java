package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.map.dto.MapPointRequest;
import stud.ntnu.krisefikser.map.dto.MapPointResponse;
import stud.ntnu.krisefikser.map.dto.UpdateMapPointRequest;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;

/**
 * Service class for managing MapPoints in the system. This class provides methods for creating,
 * updating, retrieving, and deleting MapPoints.
 */
@Service
@RequiredArgsConstructor
public class MapPointService {

  private final MapPointRepository mapPointRepository;
  private final MapPointTypeService mapPointTypeService;

  public List<MapPointResponse> getAllMapPoints() {
    return mapPointRepository.findAll().stream().map(MapPoint::toResponse).toList();
  }

  /**
   * Retrieves a MapPoint by its ID.
   *
   * @param id The ID of the MapPoint to retrieve
   * @return The MapPoint with the specified ID
   * @throws EntityNotFoundException If no MapPoint with the specified ID exists
   */
  public MapPointResponse getMapPointById(Long id) {
    MapPoint mapPoint = mapPointRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPoint not found with id: " + id));

    return mapPoint.toResponse();
  }

  /**
   * Creates a new MapPoint based on the provided request data.
   *
   * @param mapPointRequest The request data containing the information for the new MapPoint
   * @return The created MapPoint
   */
  @Transactional
  public MapPointResponse createMapPoint(MapPointRequest mapPointRequest) {
    // Verify that the referenced MapPointType exists
    MapPointType mapPointType = mapPointTypeService.getMapPointTypeById(
        mapPointRequest.getTypeId());

    MapPoint mapPoint = MapPoint.builder()
        .latitude(mapPointRequest.getLatitude())
        .longitude(mapPointRequest.getLongitude())
        .type(mapPointType)
        .build();

    return mapPointRepository.save(mapPoint).toResponse();
  }

  /**
   * Updates an existing MapPoint with the provided ID and request data.
   *
   * @param id                    The ID of the MapPoint to update
   * @param updateMapPointRequest The request data containing the updated information
   * @return The updated MapPoint
   * @throws EntityNotFoundException If no MapPoint with the specified ID exists
   */
  @Transactional
  public MapPointResponse updateMapPoint(Long id, UpdateMapPointRequest updateMapPointRequest) {
    MapPoint existingMapPoint = mapPointRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPoint not found with id: " + id));

    if (updateMapPointRequest.getLatitude() != null) {
      existingMapPoint.setLatitude(updateMapPointRequest.getLatitude());
    }

    if (updateMapPointRequest.getLongitude() != null) {
      existingMapPoint.setLongitude(updateMapPointRequest.getLongitude());
    }

    if (updateMapPointRequest.getTypeId() != null) {
      MapPointType mapPointType = mapPointTypeService.getMapPointTypeById(
          updateMapPointRequest.getTypeId());
      existingMapPoint.setType(mapPointType);
    }

    return mapPointRepository.save(existingMapPoint).toResponse();
  }

  /**
   * Deletes a MapPoint by its ID.
   *
   * @param id The ID of the MapPoint to delete
   * @throws EntityNotFoundException If no MapPoint with the specified ID exists
   */
  @Transactional
  public void deleteMapPoint(Long id) {
    if (!mapPointRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPoint not found with id: " + id);
    }
    mapPointRepository.deleteById(id);
  }
}