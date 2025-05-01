package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.MapPointResponse;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;

@Service
@RequiredArgsConstructor
public class MapPointService {

  private final MapPointRepository mapPointRepository;
  private final MapPointTypeService mapPointTypeService;

  public List<MapPointResponse> getAllMapPoints() {
    return mapPointRepository.findAll().stream().map(MapPoint::toResponse).toList();
  }

  public MapPointResponse getMapPointById(Long id) {
    MapPoint mapPoint = mapPointRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("MapPoint not found with id: " + id));

    return mapPoint.toResponse();
  }

  public MapPointResponse createMapPoint(MapPoint mapPoint) {
    // Verify that the referenced MapPointType exists
    mapPointTypeService.getMapPointTypeById(mapPoint.getType().getId());

    return mapPointRepository.save(mapPoint).toResponse();
  }

  public MapPointResponse updateMapPoint(Long id, MapPoint mapPoint) {
    if (!mapPointRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPoint not found with id: " + id);
    }
    // Verify that the referenced MapPointType exists
    mapPointTypeService.getMapPointTypeById(mapPoint.getType().getId());
    mapPoint.setId(id);
    return mapPointRepository.save(mapPoint).toResponse();
  }

  public void deleteMapPoint(Long id) {
    if (!mapPointRepository.existsById(id)) {
      throw new EntityNotFoundException("MapPoint not found with id: " + id);
    }
    mapPointRepository.deleteById(id);
  }
}