package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.MapPointTypeRequest;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.dto.UpdateMapPointTypeRequest;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;

/**
 * Service class for managing MapPointType entities. This class provides methods to create, update,
 * delete, and retrieve MapPointType entities.
 */
@Service
@RequiredArgsConstructor
public class MapPointTypeService {

  private final MapPointTypeRepository mapPointTypeRepository;

  public List<MapPointTypeResponse> getAllMapPointTypes() {
    return mapPointTypeRepository.findAll().stream().map(MapPointType::toResponse).toList();
  }

  /**
   * Retrieves a MapPointType by its ID.
   *
   * @param id The ID of the MapPointType to retrieve
   * @return The MapPointType with the specified ID
   * @throws EntityNotFoundException If no MapPointType with the specified ID exists
   */
  public MapPointType getMapPointTypeById(Long id) {
    return mapPointTypeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPointType not found with id: " + id));
  }

  /**
   * Creates a new MapPointType based on the provided request data.
   *
   * @param mapPointTypeRequest The request data containing the information for the new
   *                            MapPointType
   * @return The created MapPointType
   */
  public MapPointTypeResponse createMapPointType(MapPointTypeRequest mapPointTypeRequest) {
    MapPointType mapPointType = MapPointType.builder()
        .title(mapPointTypeRequest.getTitle())
        .iconUrl(mapPointTypeRequest.getIconUrl())
        .description(mapPointTypeRequest.getDescription())
        .openingTime(mapPointTypeRequest.getOpeningTime())
        .build();

    return mapPointTypeRepository.save(mapPointType).toResponse();
  }

  /**
   * Updates an existing MapPointType with the provided ID and request data.
   *
   * @param id                  The ID of the MapPointType to update
   * @param mapPointTypeRequest The request data containing the updated information
   * @return The updated MapPointType
   * @throws EntityNotFoundException If no MapPointType with the specified ID exists
   */
  public MapPointTypeResponse updateMapPointType(Long id,
      UpdateMapPointTypeRequest mapPointTypeRequest) {

    MapPointType mapPointType = mapPointTypeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPointType not found with id: " + id));

    if (mapPointTypeRequest.getTitle() != null) {
      mapPointType.setTitle(mapPointTypeRequest.getTitle());
    }

    if (mapPointTypeRequest.getIconUrl() != null) {
      mapPointType.setIconUrl(mapPointTypeRequest.getIconUrl());
    }

    if (mapPointTypeRequest.getDescription() != null) {
      mapPointType.setDescription(mapPointTypeRequest.getDescription());
    }

    if (mapPointTypeRequest.getOpeningTime() != null) {
      mapPointType.setOpeningTime(mapPointTypeRequest.getOpeningTime());
    }

    return mapPointTypeRepository.save(mapPointType).toResponse();
  }

  /**
   * Deletes a MapPointType by its ID.
   *
   * @param id The ID of the MapPointType to delete
   * @throws EntityNotFoundException If no MapPointType with the specified ID exists
   */
  public void deleteMapPointType(Long id) {
    if (!mapPointTypeRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPointType not found with id: " + id);
    }
    mapPointTypeRepository.deleteById(id);
  }
}