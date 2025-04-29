package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepo;
import stud.ntnu.krisefikser.user.entity.User;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseHoldMemberService {
    private final HouseholdMemberRepo householdMemberRepo;

    public List<HouseholdMember> getMembers(UUID householdId) {
        return householdMemberRepo.findByHouseholdId(householdId);
    }

    public boolean isMemberOfHousehold(User currentUser, Household household) {
        return householdMemberRepo.existsByUserAndHousehold(currentUser, household);
    }

    // TODO: Check if the user is invited
    public HouseholdMember addMember(Household household, User currentUser) {
        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setUser(currentUser);
        member.setStatus(HouseholdMemberStatus.ACCEPTED);
        return householdMemberRepo.save(member);
    }

    public void removeMember(Household household, User currentUser) {
        HouseholdMember member = householdMemberRepo.findByHouseholdAndUser(household, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        householdMemberRepo.delete(member);
    }

    public List<HouseholdMember> getHouseholdsByUser(User user) {
        return householdMemberRepo.findByUser(user);
    }
}
