package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapPointServiceTest {

    @Mock
    private MapPointRepository mapPointRepository;

    @Mock
    private MapPointTypeService mapPointTypeService;

    @InjectMocks
    private MapPointService mapPointService;

    private MapPoint testMapPoint;
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

        testMapPoint = MapPoint.builder()
                .id(1L)
                .latitude(63.4305)
                .longitude(10.3951)
                .type(testMapPointType)
                .build();
    }

    @Test
    void getAllMapPoints_ShouldReturnList() {
        List<MapPoint> expected = Arrays.asList(testMapPoint);
        when(mapPointRepository.findAll()).thenReturn(expected);

        List<MapPoint> actual = mapPointService.getAllMapPoints();

        assertEquals(expected, actual);
        verify(mapPointRepository).findAll();
    }

    @Test
    void getMapPointById_WithValidId_ShouldReturnMapPoint() {
        when(mapPointRepository.findById(1L)).thenReturn(Optional.of(testMapPoint));

        MapPoint actual = mapPointService.getMapPointById(1L);

        assertEquals(testMapPoint, actual);
        verify(mapPointRepository).findById(1L);
    }

    @Test
    void getMapPointById_WithInvalidId_ShouldThrowException() {
        when(mapPointRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> mapPointService.getMapPointById(1L));
        verify(mapPointRepository).findById(1L);
    }

    @Test
    void createMapPoint_WithValidType_ShouldReturnCreatedMapPoint() {
        when(mapPointTypeService.getMapPointTypeById(1L)).thenReturn(testMapPointType);
        when(mapPointRepository.save(any(MapPoint.class))).thenReturn(testMapPoint);

        MapPoint actual = mapPointService.createMapPoint(testMapPoint);

        assertEquals(testMapPoint, actual);
        verify(mapPointTypeService).getMapPointTypeById(1L);
        verify(mapPointRepository).save(testMapPoint);
    }

    @Test
    void createMapPoint_WithInvalidType_ShouldThrowException() {
        when(mapPointTypeService.getMapPointTypeById(1L))
                .thenThrow(new EntityNotFoundException("MapPointType not found"));

        assertThrows(EntityNotFoundException.class,
                () -> mapPointService.createMapPoint(testMapPoint));
        verify(mapPointTypeService).getMapPointTypeById(1L);
        verify(mapPointRepository, never()).save(any());
    }

    @Test
    void updateMapPoint_WithValidIdAndType_ShouldReturnUpdatedMapPoint() {
        when(mapPointRepository.existsById(1L)).thenReturn(true);
        when(mapPointTypeService.getMapPointTypeById(1L)).thenReturn(testMapPointType);
        when(mapPointRepository.save(any(MapPoint.class))).thenReturn(testMapPoint);

        MapPoint actual = mapPointService.updateMapPoint(1L, testMapPoint);

        assertEquals(testMapPoint, actual);
        verify(mapPointRepository).existsById(1L);
        verify(mapPointTypeService).getMapPointTypeById(1L);
        verify(mapPointRepository).save(testMapPoint);
    }

    @Test
    void updateMapPoint_WithInvalidId_ShouldThrowException() {
        when(mapPointRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> mapPointService.updateMapPoint(1L, testMapPoint));
        verify(mapPointRepository).existsById(1L);
        verify(mapPointTypeService, never()).getMapPointTypeById(any());
        verify(mapPointRepository, never()).save(any());
    }

    @Test
    void deleteMapPoint_WithValidId_ShouldDeleteMapPoint() {
        when(mapPointRepository.existsById(1L)).thenReturn(true);
        doNothing().when(mapPointRepository).deleteById(1L);

        mapPointService.deleteMapPoint(1L);

        verify(mapPointRepository).existsById(1L);
        verify(mapPointRepository).deleteById(1L);
    }

    @Test
    void deleteMapPoint_WithInvalidId_ShouldThrowException() {
        when(mapPointRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> mapPointService.deleteMapPoint(1L));
        verify(mapPointRepository).existsById(1L);
        verify(mapPointRepository, never()).deleteById(any());
    }
}