package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.repository.HouseholdRepo;

@Service
@RequiredArgsConstructor
public class HouseholdService {
  private HouseholdRepo householdRepo;
}
