package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepo;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseHoldMemberService {
    private final HouseholdMemberRepo householdMemberRepo;

    public List<HouseholdMember> getMembers(UUID householdId) {
        return householdMemberRepo.findByHouseholdId(householdId);
    }
}
