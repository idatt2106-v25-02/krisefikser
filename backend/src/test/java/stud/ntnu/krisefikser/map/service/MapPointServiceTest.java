package stud.ntnu.krisefikser.map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.map.dto.MapPointRequest;
import stud.ntnu.krisefikser.map.dto.MapPointResponse;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.dto.UpdateMapPointRequest;
import stud.ntnu.krisefikser.map.entity.MapPoint;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointRepository;

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
  private MapPointRequest testMapPointRequest;
  private UpdateMapPointRequest testUpdateMapPointRequest;
  private MapPointResponse testMapPointResponse;
  private MapPointTypeResponse testMapPointTypeResponse;

  @BeforeEach
  void setUp() {
    // Set up entities for repository operations
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

    // Set up DTOs for service operations
    testMapPointTypeResponse = MapPointTypeResponse.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testMapPointResponse = MapPointResponse.builder()
        .id(1L)
        .latitude(63.4305)
        .longitude(10.3951)
        .type(testMapPointTypeResponse)
        .build();

    testMapPointRequest = MapPointRequest.builder()
        .latitude(63.4305)
        .longitude(10.3951)
        .typeId(1L)
        .build();

    testUpdateMapPointRequest = UpdateMapPointRequest.builder()
        .latitude(63.4305)
        .longitude(10.3951)
        .typeId(1L)
        .build();
  }

  @Test
  void getAllMapPoints_ShouldReturnListOfMapPointResponses() {
    // Arrange
    List<MapPoint> mapPoints = List.of(testMapPoint);
    when(mapPointRepository.findAll()).thenReturn(mapPoints);

    // Act
    List<MapPointResponse> result = mapPointService.getAllMapPoints();

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testMapPoint.getId());
    assertThat(result.get(0).getLatitude()).isEqualTo(testMapPoint.getLatitude());
    assertThat(result.get(0).getLongitude()).isEqualTo(testMapPoint.getLongitude());
    verify(mapPointRepository).findAll();
  }

  @Test
  void getMapPointById_WhenExists_ShouldReturnMapPointResponse() {
    // Arrange
    when(mapPointRepository.findById(1L)).thenReturn(Optional.of(testMapPoint));

    // Act
    MapPointResponse result = mapPointService.getMapPointById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testMapPoint.getId());
    assertThat(result.getLatitude()).isEqualTo(testMapPoint.getLatitude());
    assertThat(result.getLongitude()).isEqualTo(testMapPoint.getLongitude());
    verify(mapPointRepository).findById(1L);
  }

  @Test
  void getMapPointById_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(mapPointRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> mapPointService.getMapPointById(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPoint not found with id: 1");
    verify(mapPointRepository).findById(1L);
  }

  @Test
  void createMapPoint_WithValidTypeId_ShouldReturnCreatedMapPointResponse() {
    // Arrange
    when(mapPointTypeService.getMapPointTypeById(1L)).thenReturn(testMapPointType);
    when(mapPointRepository.save(any(MapPoint.class))).thenReturn(testMapPoint);

    // Act
    MapPointResponse result = mapPointService.createMapPoint(testMapPointRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testMapPoint.getId());
    assertThat(result.getLatitude()).isEqualTo(testMapPoint.getLatitude());
    assertThat(result.getLongitude()).isEqualTo(testMapPoint.getLongitude());
    verify(mapPointTypeService).getMapPointTypeById(1L);
    verify(mapPointRepository).save(any(MapPoint.class));
  }

  @Test
  void createMapPoint_WithInvalidTypeId_ShouldThrowException() {
    // Arrange
    when(mapPointTypeService.getMapPointTypeById(1L))
        .thenThrow(new EntityNotFoundException("MapPointType not found with id: 1"));

    // Act & Assert
    assertThatThrownBy(() -> mapPointService.createMapPoint(testMapPointRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPointType not found with id: 1");
    verify(mapPointTypeService).getMapPointTypeById(1L);
    verify(mapPointRepository, never()).save(any(MapPoint.class));
  }

  @Test
  void updateMapPoint_WithValidIdAndData_ShouldReturnUpdatedMapPointResponse() {
    // Arrange
    when(mapPointRepository.findById(1L)).thenReturn(Optional.of(testMapPoint));
    when(mapPointTypeService.getMapPointTypeById(1L)).thenReturn(testMapPointType);
    when(mapPointRepository.save(any(MapPoint.class))).thenReturn(testMapPoint);

    // Act
    MapPointResponse result = mapPointService.updateMapPoint(1L, testUpdateMapPointRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testMapPoint.getId());
    assertThat(result.getLatitude()).isEqualTo(testMapPoint.getLatitude());
    assertThat(result.getLongitude()).isEqualTo(testMapPoint.getLongitude());
    verify(mapPointRepository).findById(1L);
    verify(mapPointTypeService).getMapPointTypeById(1L);
    verify(mapPointRepository).save(any(MapPoint.class));
  }

  @Test
  void updateMapPoint_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(mapPointRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> mapPointService.updateMapPoint(1L, testUpdateMapPointRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPoint not found with id: 1");
    verify(mapPointRepository).findById(1L);
    verify(mapPointRepository, never()).save(any(MapPoint.class));
  }

  @Test
  void deleteMapPoint_WithValidId_ShouldDeleteMapPoint() {
    // Arrange
    when(mapPointRepository.existsById(1L)).thenReturn(true);
    doNothing().when(mapPointRepository).deleteById(1L);

    // Act
    mapPointService.deleteMapPoint(1L);

    // Assert
    verify(mapPointRepository).existsById(1L);
    verify(mapPointRepository).deleteById(1L);
  }

  @Test
  void deleteMapPoint_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(mapPointRepository.existsById(1L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> mapPointService.deleteMapPoint(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPoint not found with id: 1");
    verify(mapPointRepository).existsById(1L);
    verify(mapPointRepository, never()).deleteById(1L);
  }
}