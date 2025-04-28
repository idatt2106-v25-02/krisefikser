package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.dto.HouseholdDto;
import stud.ntnu.krisefikser.household.dto.HouseholdMemberDto;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdRepo;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepo householdRepo;
    private final HouseHoldMemberService houseHoldMemberService;
    private final UserService userService;

    public List<HouseholdDto> getUserHouseholds() {
        User currentUser = userService.getCurrentUser();
        // Find all households where currentUser.getId()
        return householdRepo.findAll().stream()
                .map(this::convertToHouseholdDto)
                .filter(household -> household.getMembers().stream().map(HouseholdMemberDto::getUser).anyMatch(user -> user.getId() == currentUser.getId()))
                .toList();
    }

    private HouseholdDto convertToHouseholdDto(Household household) {
        List<HouseholdMember> members = houseHoldMemberService.getMembers(household.getId());

        return new HouseholdDto(
                household.getId(),
                household.getName(),
                household.getLatitude(),
                household.getLongitude(),
                household.getOwner().toDto(),
                members.stream().map(HouseholdMember::toDto).toList(),
                household.getCreatedAt()
        );
    }

    public HouseholdDto joinHousehold(UUID householdId) {
        User currentUser = userService.getCurrentUser();
        Household household = householdRepo.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        if (houseHoldMemberService.isMemberOfHousehold(currentUser, household)) {
            throw new IllegalArgumentException("Already a member of this household");
        }

        HouseholdMember member = houseHoldMemberService.addMember(household, currentUser);

        if (member == null) {
            throw new IllegalArgumentException("Failed to join household");
        }

        return convertToHouseholdDto(household);
    }
}
