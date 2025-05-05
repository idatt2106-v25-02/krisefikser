package stud.ntnu.krisefikser.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;

class WaterAmountIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private HouseholdRepository householdRepository;

  private Household testHousehold;

  @BeforeEach
  void setUp() throws Exception {
    setUpUser();
    testHousehold = getTestHousehold();
  }

  @Test
  void setWaterAmount_shouldUpdateHouseholdWaterAmount() throws Exception {
    // Arrange
    double initialWaterAmount = testHousehold.getWaterLiters();
    double newWaterAmount = 250.0;

    // Act - Set water amount
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/water/" + newWaterAmount)
            ))
        .andExpect(status().isOk());

    // Assert - Verify the water amount was updated in the database
    Household updatedHousehold = householdRepository.findById(testHousehold.getId()).orElseThrow();
    assertThat(updatedHousehold.getWaterLiters()).isEqualTo(newWaterAmount);
    assertThat(updatedHousehold.getWaterLiters()).isNotEqualTo(initialWaterAmount);
  }

  @Test
  void setWaterAmount_withZeroWater_shouldUpdateWaterAmount() throws Exception {
    // Arrange
    double newWaterAmount = 0.0;

    // Act - Set water amount to zero
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/water/" + newWaterAmount)
            ))
        .andExpect(status().isOk());

    // Assert - Verify the water amount was updated to zero
    Household updatedHousehold = householdRepository.findById(testHousehold.getId()).orElseThrow();
    assertThat(updatedHousehold.getWaterLiters()).isEqualTo(newWaterAmount);
  }

  @Test
  void setWaterAmount_withNegativeValue_shouldStillAcceptTheValue() throws Exception {
    // Note: This is a design decision - should the API reject negative values?
    // For now, testing that it accepts negative values
    
    // Arrange
    double negativeWaterAmount = -10.0;

    // Act - Set negative water amount
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/water/" + negativeWaterAmount)
            ))
        .andExpect(status().isOk());

    // Assert - Verify the water amount was updated to negative value
    Household updatedHousehold = householdRepository.findById(testHousehold.getId()).orElseThrow();
    assertThat(updatedHousehold.getWaterLiters()).isEqualTo(negativeWaterAmount);
  }
} 