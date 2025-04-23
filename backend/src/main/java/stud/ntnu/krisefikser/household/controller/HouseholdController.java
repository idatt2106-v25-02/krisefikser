package stud.ntnu.krisefikser.household.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.household.service.HouseholdService;

@RestController
@RequestMapping("/api/household")
@RequiredArgsConstructor
public class HouseholdController {
  private final HouseholdService householdService;
}
