package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stud.ntnu.krisefikser.household.dto.MeetingPointRequest;
import stud.ntnu.krisefikser.household.dto.MeetingPointResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.MeetingPoint;
import stud.ntnu.krisefikser.household.repository.MeetingPointRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingPointService {
    private final MeetingPointRepository meetingPointRepository;
    private final HouseholdService householdService;

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

    @Transactional(readOnly = true)
    public List<MeetingPointResponse> getMeetingPointsByHousehold(UUID householdId) {
        return meetingPointRepository.findByHouseholdId(householdId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public void deleteMeetingPoint(UUID id) {
        meetingPointRepository.deleteById(id);
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
}