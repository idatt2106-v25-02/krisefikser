package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapPointTypeService {
    private final MapPointTypeRepository mapPointTypeRepository;

    public List<MapPointType> getAllMapPointTypes() {
        return mapPointTypeRepository.findAll();
    }

    public MapPointType getMapPointTypeById(Long id) {
        return mapPointTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MapPointType not found with id: " + id));
    }

    public MapPointType createMapPointType(MapPointType mapPointType) {
        return mapPointTypeRepository.save(mapPointType);
    }

    public MapPointType updateMapPointType(Long id, MapPointType mapPointType) {
        if (!mapPointTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("MapPointType not found with id: " + id);
        }

        // Fetch the existing entity
        MapPointType existingType = mapPointTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MapPointType not found with id: " + id));

        // Update only the fields that should be changed
        existingType.setTitle(mapPointType.getTitle());
        existingType.setDescription(mapPointType.getDescription());
        existingType.setIconUrl(mapPointType.getIconUrl());
        existingType.setOpeningTime(mapPointType.getOpeningTime());

        return mapPointTypeRepository.save(existingType);
    }

    public void deleteMapPointType(Long id) {
        if (!mapPointTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("MapPointType not found with id: " + id);
        }
        mapPointTypeRepository.deleteById(id);
    }
}