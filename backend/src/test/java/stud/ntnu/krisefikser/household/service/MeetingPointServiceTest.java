package stud.ntnu.krisefikser.household.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.household.dto.MeetingPointRequest;
import stud.ntnu.krisefikser.household.dto.MeetingPointResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.MeetingPoint;
import stud.ntnu.krisefikser.household.repository.MeetingPointRepository;
import stud.ntnu.krisefikser.user.entity.User;

@ExtendWith(MockitoExtension.class)
class MeetingPointServiceTest {

  @Mock
  private MeetingPointRepository meetingPointRepository;

  @Mock
  private HouseholdService householdService;

  @InjectMocks
  private MeetingPointService meetingPointService;

  private UUID householdId;
  private UUID meetingPointId;
  private Household testHousehold;
  private MeetingPoint testMeetingPoint;
  private MeetingPointRequest meetingPointRequest;

  @BeforeEach
  void setUp() {
    // Set up test data
    householdId = UUID.randomUUID();
    meetingPointId = UUID.randomUUID();

    User owner = User.builder()
        .id(UUID.randomUUID())
        .email("owner@example.com")
        .firstName("Owner")
        .lastName("User")
        .build();

    testHousehold = Household.builder()
        .id(householdId)
        .name("Test Household")
        .owner(owner)
        .address("Test Address")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .build();

    testMeetingPoint = MeetingPoint.builder()
        .id(meetingPointId)
        .name("Test Meeting Point")
        .description("Test Description")
        .latitude(63.4350)
        .longitude(10.4000)
        .household(testHousehold)
        .build();

    meetingPointRequest = MeetingPointRequest.builder()
        .name("Test Meeting Point")
        .description("Test Description")
        .latitude(63.4350)
        .longitude(10.4000)
        .build();
  }

  @Test
  void createMeetingPoint_ShouldCreateAndReturnPoint() {
    // Arrange
    when(householdService.getHouseholdById(householdId)).thenReturn(testHousehold);
    when(meetingPointRepository.save(any(MeetingPoint.class))).thenReturn(testMeetingPoint);

    // Act
    MeetingPointResponse result = meetingPointService.createMeetingPoint(householdId,
        meetingPointRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(meetingPointId);
    assertThat(result.getName()).isEqualTo(meetingPointRequest.getName());
    assertThat(result.getDescription()).isEqualTo(meetingPointRequest.getDescription());
    assertThat(result.getLatitude()).isEqualTo(meetingPointRequest.getLatitude());
    assertThat(result.getLongitude()).isEqualTo(meetingPointRequest.getLongitude());
    assertThat(result.getHouseholdId()).isEqualTo(householdId);

    verify(householdService).getHouseholdById(householdId);
    verify(meetingPointRepository).save(any(MeetingPoint.class));
  }

  @Test
  void getMeetingPointsByHousehold_ShouldReturnPoints() {
    // Arrange
    List<MeetingPoint> meetingPoints = Arrays.asList(testMeetingPoint);
    when(meetingPointRepository.findByHouseholdId(householdId)).thenReturn(meetingPoints);

    // Act
    List<MeetingPointResponse> result = meetingPointService.getMeetingPointsByHousehold(
        householdId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(meetingPointId);
    assertThat(result.get(0).getName()).isEqualTo(testMeetingPoint.getName());
    assertThat(result.get(0).getHouseholdId()).isEqualTo(householdId);

    verify(meetingPointRepository).findByHouseholdId(householdId);
  }

  @Test
  void updateMeetingPoint_WhenExists_ShouldUpdateAndReturnPoint() {
    // Arrange
    when(meetingPointRepository.findById(meetingPointId)).thenReturn(Optional.of(testMeetingPoint));

    MeetingPointRequest updateRequest = MeetingPointRequest.builder()
        .name("Updated Meeting Point")
        .description("Updated Description")
        .latitude(63.4400)
        .longitude(10.4100)
        .build();

    MeetingPoint updatedPoint = MeetingPoint.builder()
        .id(meetingPointId)
        .name(updateRequest.getName())
        .description(updateRequest.getDescription())
        .latitude(updateRequest.getLatitude())
        .longitude(updateRequest.getLongitude())
        .household(testHousehold)
        .build();

    when(meetingPointRepository.save(any(MeetingPoint.class))).thenReturn(updatedPoint);

    // Act
    MeetingPointResponse result = meetingPointService.updateMeetingPoint(meetingPointId,
        updateRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(meetingPointId);
    assertThat(result.getName()).isEqualTo(updateRequest.getName());
    assertThat(result.getDescription()).isEqualTo(updateRequest.getDescription());
    assertThat(result.getLatitude()).isEqualTo(updateRequest.getLatitude());
    assertThat(result.getLongitude()).isEqualTo(updateRequest.getLongitude());

    verify(meetingPointRepository).findById(meetingPointId);
    verify(meetingPointRepository).save(any(MeetingPoint.class));
  }

  @Test
  void updateMeetingPoint_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(meetingPointRepository.findById(meetingPointId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(
        () -> meetingPointService.updateMeetingPoint(meetingPointId, meetingPointRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Meeting point not found");

    verify(meetingPointRepository).findById(meetingPointId);
  }

  @Test
  void deleteMeetingPoint_ShouldDeletePoint() {
    // Act
    meetingPointService.deleteMeetingPoint(meetingPointId);

    // Assert
    verify(meetingPointRepository).deleteById(meetingPointId);
  }
}