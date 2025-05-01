package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepository householdRepo;
    private final HouseholdMemberService householdMemberService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<HouseholdResponse> getUserHouseholds() {
        User currentUser = userService.getCurrentUser();
        List<HouseholdMember> userMemberships = householdMemberService.getHouseholdsByUser(currentUser);

        return userMemberships.stream()
                .map(member -> toHouseholdResponse(member.getHousehold()))
                .toList();
    }

    private HouseholdResponse toHouseholdResponse(Household household) {
        User currentUser = userService.getCurrentUser();
        List<HouseholdMember> members = householdMemberService.getMembers(household.getId());

        boolean isActive = false;
        if (currentUser.getActiveHousehold() != null) {
            isActive = household.getId().equals(currentUser.getActiveHousehold().getId());
        }

        return new HouseholdResponse(
                household.getId(),
                household.getName(),
                household.getLatitude(),
                household.getLongitude(),
                household.getAddress(),
                household.getOwner().toDto(),
                members.stream().map(HouseholdMember::toDto).toList(),
                household.getCreatedAt(),
                isActive);
    }

    @Transactional
    public HouseholdResponse joinHousehold(UUID householdId) {
        User currentUser = userService.getCurrentUser();
        Household household = householdRepo.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        if (householdMemberService.isMemberOfHousehold(currentUser, household)) {
            throw new IllegalArgumentException("Already a member of this household");
        }

        HouseholdMember member = householdMemberService.addMember(household, currentUser);

        if (member == null) {
            throw new IllegalArgumentException("Failed to join household");
        }

        return toHouseholdResponse(household);
    }

    @Transactional
    public HouseholdResponse setActiveHousehold(UUID householdId) {
        User currentUser = userService.getCurrentUser();
        Household household = householdRepo.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        if (!householdMemberService.isMemberOfHousehold(currentUser, household)) {
            throw new IllegalArgumentException("Not a member of this household");
        }

        currentUser.setActiveHousehold(household);
        userService.updateActiveHousehold(household);

        return toHouseholdResponse(household);
    }

    public HouseholdResponse getActiveHousehold() {
        User currentUser = userService.getCurrentUser();
        Household household = currentUser.getActiveHousehold();

        if (household == null) {
            throw new IllegalArgumentException("No active household found");
        }

        return toHouseholdResponse(household);
    }

    @Transactional
    public void leaveHousehold(UUID householdId) {
        User currentUser = userService.getCurrentUser();
        Household household = householdRepo.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        if (!householdMemberService.isMemberOfHousehold(currentUser, household)) {
            throw new IllegalArgumentException("Not a member of this household");
        }

        userService.updateActiveHousehold(null);
        householdMemberService.removeMember(household, currentUser);
    }

    @Transactional
    public void deleteHousehold(UUID id) {
        User currentUser = userService.getCurrentUser();
        Household household = householdRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        // Check if current user is the owner
        if (!household.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only the owner can delete a household");
        }

        // Clear active household for all members
        List<HouseholdMember> members = householdMemberService.getMembers(id);
        for (HouseholdMember member : members) {
            if (member.getUser().getActiveHousehold() != null &&
                    member.getUser().getActiveHousehold().getId().equals(id)) {
                userService.updateActiveHousehold(null);
            }
            householdMemberService.removeMember(household, member.getUser());
        }

        householdRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Household getHouseholdById(UUID id) {
        return householdRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));
    }

    @Transactional
    public HouseholdResponse createHousehold(CreateHouseholdRequest household) {
        User currentUser = userService.getCurrentUser();
        Household newHousehold = new Household();
        newHousehold.setName(household.getName());
        newHousehold.setLatitude(household.getLatitude());
        newHousehold.setLongitude(household.getLongitude());
        newHousehold.setAddress(household.getAddress());
        newHousehold.setPostalCode(household.getPostalCode());
        newHousehold.setCity(household.getCity());
        newHousehold.setOwner(currentUser);

        householdRepo.save(newHousehold);
        householdMemberService.addMember(newHousehold, currentUser);

        currentUser.setActiveHousehold(newHousehold);
        userService.updateActiveHousehold(newHousehold);

        return toHouseholdResponse(newHousehold);
    }
}
