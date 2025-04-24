package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepo;

@Service
@RequiredArgsConstructor
public class HouseHoldMemberService {
  private final HouseholdMemberRepo householdMemberRepo;
}
