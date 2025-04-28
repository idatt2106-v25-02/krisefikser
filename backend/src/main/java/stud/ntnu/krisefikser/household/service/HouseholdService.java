package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.dto.HouseholdDto;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdRepo;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepo householdRepo;
    private final HouseHoldMemberService houseHoldMemberService;
    private final UserService userService;

    public List<HouseholdDto> getUserHouseholds() {
        User currentUser = userService.getCurrentUser();
        // Find all households where currentUser.getId()
        List<Household> households = householdRepo.findAll();
        List<HouseholdDto> = households.stream()
                .filter(household -> convertToHouseholdDto(household).getMembers().contains(currentUser))
                .map(household -> new HouseholdDto(household.getId(), household.getName()))
                .toList();

        return List.of();
    }

    private HouseholdDto convertToHouseholdDto(Household household) {
        List<HouseholdMember> members = houseHoldMemberService.getMembers(household.getId());

        return new HouseholdDto(
                household.getId(),
                household.getName(),
                household.getLatitude(),
                household.getLongitude(),
                household.getOwner(),
                household.stream().map(member -> member.toDto())
                household.getCreatedAt()
        );
    }
}
