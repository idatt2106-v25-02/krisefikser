package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;

@Service
@RequiredArgsConstructor
public class MapPointTypeService {

  private final MapPointTypeRepository mapPointTypeRepository;

  public List<MapPointTypeResponse> getAllMapPointTypes() {
    return mapPointTypeRepository.findAll().stream().map(MapPointType::toResponse).toList();
  }

  public MapPointTypeResponse getMapPointTypeById(Long id) {
    MapPointType mapPointType = mapPointTypeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPointType not found with id: " + id));

    return mapPointType.toResponse();
  }

  public MapPointTypeResponse createMapPointType(MapPointType mapPointType) {
    return mapPointTypeRepository.save(mapPointType).toResponse();
  }

  public MapPointTypeResponse updateMapPointType(Long id, MapPointType mapPointType) {
    if (!mapPointTypeRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPointType not found with id: " + id);
    }
    mapPointType.setId(id);
    return mapPointTypeRepository.save(mapPointType).toResponse();
  }

  public void deleteMapPointType(Long id) {
    if (!mapPointTypeRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPointType not found with id: " + id);
    }
    mapPointTypeRepository.deleteById(id);
  }
}