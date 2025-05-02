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
import stud.ntnu.krisefikser.map.dto.MapPointTypeRequest;
import stud.ntnu.krisefikser.map.dto.MapPointTypeResponse;
import stud.ntnu.krisefikser.map.dto.UpdateMapPointTypeRequest;
import stud.ntnu.krisefikser.map.entity.MapPointType;
import stud.ntnu.krisefikser.map.repository.MapPointTypeRepository;

@ExtendWith(MockitoExtension.class)
class MapPointTypeServiceTest {

  @Mock
  private MapPointTypeRepository mapPointTypeRepository;

  @InjectMocks
  private MapPointTypeService mapPointTypeService;

  private MapPointType testMapPointType;
  private MapPointTypeRequest testMapPointTypeRequest;
  private UpdateMapPointTypeRequest testUpdateMapPointTypeRequest;
  private MapPointTypeResponse testMapPointTypeResponse;

  @BeforeEach
  void setUp() {
    // Set up entity for repository operations
    testMapPointType = MapPointType.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    // Set up DTOs for service operations
    testMapPointTypeResponse = MapPointTypeResponse.builder()
        .id(1L)
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testMapPointTypeRequest = MapPointTypeRequest.builder()
        .title("Test Point Type")
        .iconUrl("http://example.com/icon.png")
        .description("Test Description")
        .openingTime("9:00-17:00")
        .build();

    testUpdateMapPointTypeRequest = UpdateMapPointTypeRequest.builder()
        .title("Updated Point Type")
        .iconUrl("http://example.com/updated-icon.png")
        .description("Updated Description")
        .openingTime("10:00-18:00")
        .build();
  }

  @Test
  void getAllMapPointTypes_ShouldReturnListOfMapPointTypeResponses() {
    // Arrange
    List<MapPointType> mapPointTypes = List.of(testMapPointType);
    when(mapPointTypeRepository.findAll()).thenReturn(mapPointTypes);

    // Act
    List<MapPointTypeResponse> result = mapPointTypeService.getAllMapPointTypes();

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testMapPointType.getId());
    assertThat(result.get(0).getTitle()).isEqualTo(testMapPointType.getTitle());
    assertThat(result.get(0).getIconUrl()).isEqualTo(testMapPointType.getIconUrl());
    verify(mapPointTypeRepository).findAll();
  }

  @Test
  void getMapPointTypeById_WhenExists_ShouldReturnMapPointType() {
    // Arrange
    when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.of(testMapPointType));

    // Act
    MapPointType result = mapPointTypeService.getMapPointTypeById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testMapPointType.getId());
    assertThat(result.getTitle()).isEqualTo(testMapPointType.getTitle());
    assertThat(result.getIconUrl()).isEqualTo(testMapPointType.getIconUrl());
    verify(mapPointTypeRepository).findById(1L);
  }

  @Test
  void getMapPointTypeById_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> mapPointTypeService.getMapPointTypeById(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPointType not found with id: 1");
    verify(mapPointTypeRepository).findById(1L);
  }

  @Test
  void createMapPointType_ShouldReturnCreatedMapPointTypeResponse() {
    // Arrange
    when(mapPointTypeRepository.save(any(MapPointType.class))).thenReturn(testMapPointType);

    // Act
    MapPointTypeResponse result = mapPointTypeService.createMapPointType(testMapPointTypeRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testMapPointType.getId());
    assertThat(result.getTitle()).isEqualTo(testMapPointType.getTitle());
    assertThat(result.getIconUrl()).isEqualTo(testMapPointType.getIconUrl());
    verify(mapPointTypeRepository).save(any(MapPointType.class));
  }

  @Test
  void updateMapPointType_WithValidId_ShouldReturnUpdatedMapPointTypeResponse() {
    // Arrange
    when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.of(testMapPointType));

    // Create an updated entity that would be returned from the repository
    MapPointType updatedMapPointType = MapPointType.builder()
        .id(1L)
        .title("Updated Point Type")
        .iconUrl("http://example.com/updated-icon.png")
        .description("Updated Description")
        .openingTime("10:00-18:00")
        .build();

    when(mapPointTypeRepository.save(any(MapPointType.class))).thenReturn(updatedMapPointType);

    // Act
    MapPointTypeResponse result = mapPointTypeService.updateMapPointType(1L,
        testUpdateMapPointTypeRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(updatedMapPointType.getId());
    assertThat(result.getTitle()).isEqualTo(updatedMapPointType.getTitle());
    assertThat(result.getIconUrl()).isEqualTo(updatedMapPointType.getIconUrl());
    verify(mapPointTypeRepository).findById(1L);
    verify(mapPointTypeRepository).save(any(MapPointType.class));
  }

  @Test
  void updateMapPointType_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(
        () -> mapPointTypeService.updateMapPointType(1L, testUpdateMapPointTypeRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPointType not found with id: 1");
    verify(mapPointTypeRepository).findById(1L);
    verify(mapPointTypeRepository, never()).save(any(MapPointType.class));
  }

  @Test
  void deleteMapPointType_WithValidId_ShouldDeleteMapPointType() {
    // Arrange
    when(mapPointTypeRepository.existsById(1L)).thenReturn(true);
    doNothing().when(mapPointTypeRepository).deleteById(1L);

    // Act
    mapPointTypeService.deleteMapPointType(1L);

    // Assert
    verify(mapPointTypeRepository).existsById(1L);
    verify(mapPointTypeRepository).deleteById(1L);
  }

  @Test
  void deleteMapPointType_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(mapPointTypeRepository.existsById(1L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> mapPointTypeService.deleteMapPointType(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MapPointType not found with id: 1");
    verify(mapPointTypeRepository).existsById(1L);
    verify(mapPointTypeRepository, never()).deleteById(1L);
  }
}