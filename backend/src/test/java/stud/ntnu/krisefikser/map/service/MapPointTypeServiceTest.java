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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;

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
    void getAllMapPointTypes_ShouldReturnAllTypes() {
        // Arrange
        List<MapPointType> expected = List.of(testMapPointType);
        when(mapPointTypeRepository.findAll()).thenReturn(expected);

        // Act
        List<MapPointType> result = mapPointTypeService.getAllMapPointTypes();

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst()).isEqualTo(testMapPointType);
        verify(mapPointTypeRepository).findAll();
    }

    @Test
    void getMapPointTypeById_WithValidId_ShouldReturnMapPointType() {
        // Arrange
        when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.of(testMapPointType));

        // Act
        MapPointType result = mapPointTypeService.getMapPointTypeById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testMapPointType);
        verify(mapPointTypeRepository).findById(1L);
    }

    @Test
    void getMapPointTypeById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> mapPointTypeService.getMapPointTypeById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("MapPointType not found with id: 1");
        verify(mapPointTypeRepository).findById(1L);
    }

    @Test
    void createMapPointType_ShouldReturnCreatedMapPointType() {
        // Arrange
        when(mapPointTypeRepository.save(any(MapPointType.class))).thenReturn(testMapPointType);

        // Act
        MapPointType result = mapPointTypeService.createMapPointType(testMapPointType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testMapPointType);
        verify(mapPointTypeRepository).save(testMapPointType);
    }

    @Test
    void updateMapPointType_WithValidId_ShouldReturnUpdatedMapPointType() {
        // Arrange
        when(mapPointTypeRepository.existsById(1L)).thenReturn(true);
        when(mapPointTypeRepository.findById(1L)).thenReturn(Optional.of(testMapPointType));
        when(mapPointTypeRepository.save(any(MapPointType.class))).thenReturn(testMapPointType);

        // Act
        MapPointType result = mapPointTypeService.updateMapPointType(1L, testMapPointType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testMapPointType);
        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository).findById(1L);
        verify(mapPointTypeRepository).save(testMapPointType);
    }

    @Test
    void updateMapPointType_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(mapPointTypeRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> mapPointTypeService.updateMapPointType(1L, testMapPointType))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("MapPointType not found with id: 1");
        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository, never()).save(any());
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
                .hasMessage("MapPointType not found with id: 1");
        verify(mapPointTypeRepository).existsById(1L);
        verify(mapPointTypeRepository, never()).deleteById(any());
    }
}