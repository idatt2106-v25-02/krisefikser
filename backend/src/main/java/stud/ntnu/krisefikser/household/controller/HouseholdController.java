package stud.ntnu.krisefikser.household.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.household.service.HouseholdService;

/**
 * REST controller for managing households in the system.
 * Provides endpoints for household management operations.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
@Tag(name = "Household", description = "Household management APIs")
public class HouseholdController {
  /**
   * The household service for handling household-related operations.
   */
  private final HouseholdService householdService;
}
