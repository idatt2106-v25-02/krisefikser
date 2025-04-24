package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapPointService {
    private final MapPointRepository mapPointRepository;
    private final MapPointTypeService mapPointTypeService;

    public List<MapPoint> getAllMapPoints() {
        return mapPointRepository.findAll();
    }

    public MapPoint getMapPointById(Long id) {
        return mapPointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MapPoint not found with id: " + id));
    }

    public MapPoint createMapPoint(MapPoint mapPoint) {
        // Verify that the referenced MapPointType exists
        mapPointTypeService.getMapPointTypeById(mapPoint.getType().getId());
        return mapPointRepository.save(mapPoint);
    }

    public MapPoint updateMapPoint(Long id, MapPoint mapPoint) {
        if (!mapPointRepository.existsById(id)) {
            throw new EntityNotFoundException("MapPoint not found with id: " + id);
        }
        // Verify that the referenced MapPointType exists
        mapPointTypeService.getMapPointTypeById(mapPoint.getType().getId());
        mapPoint.setId(id);
        return mapPointRepository.save(mapPoint);
    }

    public void deleteMapPoint(Long id) {
        if (!mapPointRepository.existsById(id)) {
            throw new EntityNotFoundException("MapPoint not found with id: " + id);
        }
        mapPointRepository.deleteById(id);
    }
}