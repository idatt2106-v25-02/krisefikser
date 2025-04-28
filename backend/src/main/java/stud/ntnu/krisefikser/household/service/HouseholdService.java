package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.data.HouseholdResponse;
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

    public List<HouseholdResponse> getUserHouseholds() {
        User currentUser = userService.getCurrentUser();
        // Find all households where currentUser.getId()
        List<Household> households = householdRepo.findAll();
        List<HouseholdResponse> = households.stream()
                .filter(household -> household.getMembers().contains(currentUser))
                .map(household -> new HouseholdResponse(household.getId(), household.getName()))
                .toList();

        return List.of();
    }

    private HouseholdResponse convertToHouseholdResponse(Household household) {
        List<HouseholdMember> members =
        return new HouseholdResponse(
                household.getId(),
                household.getName(),
                household.getLatitude(),
                household.getLongitude(),
                household.getOwner(),
                household.getMembers(),
                household.getCreatedAt()
        );
    }
}
