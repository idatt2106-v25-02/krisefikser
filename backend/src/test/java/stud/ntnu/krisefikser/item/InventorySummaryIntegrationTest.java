package stud.ntnu.krisefikser.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistCategory;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;

public class InventorySummaryIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FoodItemRepository foodItemRepository;

  @Autowired
  private ChecklistItemRepository checklistItemRepository;

  @Autowired
  private HouseholdRepository householdRepository;

  private Household testHousehold;

  @BeforeEach
  void setUp() throws Exception {
    setUpUser();
    testHousehold = getTestHousehold();

    // Clear any existing items
    foodItemRepository.deleteAll();
    checklistItemRepository.deleteAll();

    // Set water amount
    testHousehold.setWaterLiters(10.0);
    householdRepository.save(testHousehold);
  }

  @Test
  void getInventorySummary_ShouldReturnCorrectSummary() throws Exception {
    // Arrange - Create food items
    CreateFoodItemRequest foodItem1 = TestDataFactory.createTestFoodItemRequest(
        "Food Item 1",
        1000,
        Instant.now().plusSeconds(86400)
    );

    CreateFoodItemRequest foodItem2 = TestDataFactory.createTestFoodItemRequest(
        "Food Item 2",
        2000,
        Instant.now().plusSeconds(86400)
    );

    // Add food items
    mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(foodItem1))
            ))
        .andExpect(status().isCreated());

    mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(foodItem2))
            ))
        .andExpect(status().isCreated());

    // Create and add checklist items - 2 checked, 1 unchecked
    ChecklistItem checkedItem1 = TestDataFactory.createTestChecklistItem(
        "Checked Item 1",
        ChecklistCategory.HEALTH_HYGIENE,
        true,
        testHousehold
    );

    ChecklistItem checkedItem2 = TestDataFactory.createTestChecklistItem(
        "Checked Item 2",
        ChecklistCategory.HEAT_LIGHT,
        true,
        testHousehold
    );

    ChecklistItem uncheckedItem = TestDataFactory.createTestChecklistItem(
        "Unchecked Item",
        ChecklistCategory.OTHER,
        false,
        testHousehold
    );

    checklistItemRepository.save(checkedItem1);
    checklistItemRepository.save(checkedItem2);
    checklistItemRepository.save(uncheckedItem);

    // Act & Assert - Get summary
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/summary")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kcal").value(3000)) // Total kcal of all food items
        .andExpect(jsonPath("$.kcalGoal").exists()) // Should have a goal value
        .andExpect(jsonPath("$.waterLiters").value(10.0)) // Set water amount
        .andExpect(jsonPath("$.waterLitersGoal").exists()) // Should have a goal value
        .andExpect(jsonPath("$.checkedItems").value(2)) // 2 checked items
        .andExpect(jsonPath("$.totalItems").value(3)); // 3 total items
  }

  @Test
  void getInventorySummary_WithNoItems_ShouldReturnZeroValues() throws Exception {
    // Act & Assert - Get summary with no items
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/summary")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kcal").value(0)) // No food items
        .andExpect(jsonPath("$.kcalGoal").exists()) // Should have a goal value
        .andExpect(jsonPath("$.waterLiters").value(10.0)) // Set water amount
        .andExpect(jsonPath("$.waterLitersGoal").exists()) // Should have a goal value
        .andExpect(jsonPath("$.checkedItems").value(0)) // No checked items
        .andExpect(jsonPath("$.totalItems").value(0)); // No total items
  }

  @Test
  void getInventorySummary_WhenNoAuthentication_ShouldReturnUnauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(
            get("/api/items/summary")
        )
        .andExpect(status().isUnauthorized());
  }
} 