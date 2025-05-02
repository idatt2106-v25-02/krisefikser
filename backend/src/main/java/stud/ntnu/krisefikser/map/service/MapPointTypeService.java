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

@Service
@RequiredArgsConstructor
public class MapPointTypeService {

  private final MapPointTypeRepository mapPointTypeRepository;

  public List<MapPointTypeResponse> getAllMapPointTypes() {
    return mapPointTypeRepository.findAll().stream().map(MapPointType::toResponse).toList();
  }

  public MapPointType getMapPointTypeById(Long id) {
    MapPointType mapPointType = mapPointTypeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPointType not found with id: " + id));

    return mapPointType;
  }

  public MapPointTypeResponse createMapPointType(MapPointTypeRequest mapPointTypeRequest) {
    MapPointType mapPointType = MapPointType.builder()
        .title(mapPointTypeRequest.getTitle())
        .iconUrl(mapPointTypeRequest.getIconUrl())
        .description(mapPointTypeRequest.getDescription())
        .openingTime(mapPointTypeRequest.getOpeningTime())
        .build();

    return mapPointTypeRepository.save(mapPointType).toResponse();
  }

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

  public void deleteMapPointType(Long id) {
    if (!mapPointTypeRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPointType not found with id: " + id);
    }
    mapPointTypeRepository.deleteById(id);
  }
}