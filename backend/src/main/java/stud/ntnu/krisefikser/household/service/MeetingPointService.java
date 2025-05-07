package stud.ntnu.krisefikser.household.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.dto.MeetingPointRequest;
import stud.ntnu.krisefikser.household.dto.MeetingPointResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.MeetingPoint;
import stud.ntnu.krisefikser.household.repository.MeetingPointRepository;

/**
 * Service class for managing meeting points within households. Provides methods to create, update,
 * delete, and retrieve meeting points.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class MeetingPointService {

  private final MeetingPointRepository meetingPointRepository;
  private final HouseholdService householdService;

  /**
   * Creates a new meeting point for the specified household.
   *
   * @param householdId the ID of the household
   * @param request     the meeting point data
   * @return MeetingPointResponse containing the created meeting point
   */
  @Transactional
  public MeetingPointResponse createMeetingPoint(UUID householdId, MeetingPointRequest request) {
    Household household = householdService.getHouseholdById(householdId);

    MeetingPoint meetingPoint = MeetingPoint.builder()
        .name(request.getName())
        .description(request.getDescription())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .household(household)
        .build();

    meetingPoint = meetingPointRepository.save(meetingPoint);
    return mapToResponse(meetingPoint);
  }

  private MeetingPointResponse mapToResponse(MeetingPoint meetingPoint) {
    return MeetingPointResponse.builder()
        .id(meetingPoint.getId())
        .name(meetingPoint.getName())
        .description(meetingPoint.getDescription())
        .latitude(meetingPoint.getLatitude())
        .longitude(meetingPoint.getLongitude())
        .householdId(meetingPoint.getHousehold().getId())
        .build();
  }

  /**
   * Retrieves all meeting points for the specified household.
   *
   * @param householdId the ID of the household
   * @return a list of MeetingPointResponse containing all meeting points for the household
   */
  @Transactional(readOnly = true)
  public List<MeetingPointResponse> getMeetingPointsByHousehold(UUID householdId) {
    return meetingPointRepository.findByHouseholdId(householdId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Updates an existing meeting point.
   *
   * @param id      the ID of the meeting point
   * @param request the updated meeting point data
   * @return MeetingPointResponse containing the updated meeting point
   */
  @Transactional
  public MeetingPointResponse updateMeetingPoint(UUID id, MeetingPointRequest request) {
    MeetingPoint meetingPoint = meetingPointRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Meeting point not found"));

    meetingPoint.setName(request.getName());
    meetingPoint.setDescription(request.getDescription());
    meetingPoint.setLatitude(request.getLatitude());
    meetingPoint.setLongitude(request.getLongitude());

    meetingPoint = meetingPointRepository.save(meetingPoint);
    return mapToResponse(meetingPoint);
  }

  /**
   * Deletes a meeting point by its ID.
   *
   * @param id the ID of the meeting point to delete
   */
  @Transactional
  public void deleteMeetingPoint(UUID id) {
    meetingPointRepository.deleteById(id);
  }
}