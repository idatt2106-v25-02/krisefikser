package stud.ntnu.krisefikser.household.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.data.HouseholdResponse;
import stud.ntnu.krisefikser.household.repository.HouseholdRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private HouseholdRepo householdRepo;

    public List<HouseholdResponse> getAllHouseholds() {
        return List.of();
    }
}
