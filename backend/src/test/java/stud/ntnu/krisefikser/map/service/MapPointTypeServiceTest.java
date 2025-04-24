package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapPointTypeServiceTest {

    @Mock
    private MapPointTypeRepository mapPointTypeRepository;

    @InjectMocks
    private MapPointTypeService mapPointTypeService;

    private MapPointType testMapPointType;

    @BeforeEach
    void setUp() {
        testMapPointType = MapPointType.builder()
                .id(1L)
                .title("Test Point Type")
                .iconUrl("http://example.com/icon.png")
                .description("Test Description")
                .openingTime("9:00-17:00")
                .build();
    }

    @Test
    void getAllMapPointTypes_ShouldReturnList() {
        List<MapPointType> expected = Arrays.asList(testMapPointType);
        when(mapPointTypeRepository.findAll()).thenReturn(expected);

        List<MapPointType> actual = mapPointTypeService.getAllMapPointTypes();

        assertEquals(expected, actual);
        verify(mapPointTypeRepository).findAll();
    }

    @Test
    void getMapPointTypeById_WithValidId_ShouldReturnMapPointType() {
        when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.of(testMapPointType));

        MapPointType actual = mapPointTypeService.getMapPointTypeById(1L);

        assertEquals(testMapPointType, actual);
        verify(mapPointTypeRepository).findById(1L);
    }

    @Test
    void getMapPointTypeById_WithInvalidId_ShouldThrowException() {
        when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> mapPointTypeService.getMapPointTypeById(1L));
        verify(mapPointTypeRepository).findById(1L);
    }

    @Test
    void createMapPointType_ShouldReturnCreatedMapPointType() {
        when(mapPointTypeRepository.save(any(MapPointType.class))).thenReturn(testMapPointType);

        MapPointType actual = mapPointTypeService.createMapPointType(testMapPointType);

        assertEquals(testMapPointType, actual);
        verify(mapPointTypeRepository).save(testMapPointType);
    }

    @Test
    void updateMapPointType_WithValidId_ShouldReturnUpdatedMapPointType() {
        when(mapPointTypeRepository.existsById(1L)).thenReturn(true);
        when(mapPointTypeRepository.save(any(MapPointType.class))).thenReturn(testMapPointType);

        MapPointType actual = mapPointTypeService.updateMapPointType(1L, testMapPointType);

        assertEquals(testMapPointType, actual);
        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository).save(testMapPointType);
    }

    @Test
    void updateMapPointType_WithInvalidId_ShouldThrowException() {
        when(mapPointTypeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> mapPointTypeService.updateMapPointType(1L, testMapPointType));
        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository, never()).save(any());
    }

    @Test
    void deleteMapPointType_WithValidId_ShouldDeleteMapPointType() {
        when(mapPointTypeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(mapPointTypeRepository).deleteById(1L);

        mapPointTypeService.deleteMapPointType(1L);

        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository).deleteById(1L);
    }

    @Test
    void deleteMapPointType_WithInvalidId_ShouldThrowException() {
        when(mapPointTypeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> mapPointTypeService.deleteMapPointType(1L));
        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository, never()).deleteById(any());
    }
}