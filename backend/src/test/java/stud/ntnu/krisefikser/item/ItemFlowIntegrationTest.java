package stud.ntnu.krisefikser.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistType;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;

class ItemFlowIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FoodItemRepository foodItemRepository;

  @Autowired
  private ChecklistItemRepository checklistItemRepository;

  @BeforeEach
  void setUp() throws Exception {
    setUpUser();
  }

  @Test
  void completeItemFlow() throws Exception {
    // 1. Create a food item - using TestDataFactory
    CreateFoodItemRequest createRequest = TestDataFactory.createTestFoodItemRequest(
        "Test Food Item",
        100,
        Instant.now().plusSeconds(86400) // 1 day from now
    );

    // Use JWT authentication with the token from setup
    MvcResult createResult = mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            ))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Test Food Item"))
        .andReturn();

    String responseContent = createResult.getResponse().getContentAsString();
    UUID foodItemId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

    // 2. Get all food items for the household
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/food")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(foodItemId.toString()))
        .andExpect(jsonPath("$[0].name").value("Test Food Item"));

    // 3. Create boundary test for expiration date - past date
    CreateFoodItemRequest pastDateRequest = TestDataFactory.createTestFoodItemRequest(
        "Past Date Item",
        200,
        Instant.now().minusSeconds(86400) // 1 day ago
    );

    mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pastDateRequest))
            ))
        .andExpect(status().isCreated());

    // 4. Create boundary test for expiration date - far future
    CreateFoodItemRequest farFutureRequest = TestDataFactory.createTestFoodItemRequest(
        "Far Future Item",
        300,
        Instant.now().plusSeconds(86400 * 365 * 10) // 10 years from now
    );

    mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(farFutureRequest))
            ))
        .andExpect(status().isCreated());

    // 5. Create checklist items using TestDataFactory
    ChecklistItem checklistItem = TestDataFactory.createTestChecklistItem(
        "Test Checklist Item",
        ChecklistType.HEALTH,
        false,
        getTestHousehold()
    );

    checklistItemRepository.save(checklistItem);

    // 6. Get all checklist items for the household
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/checklist")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Test Checklist Item"))
        .andExpect(jsonPath("$[0].checked").value(false));

    // 7. Toggle checklist item status - Use the correct endpoint path as defined in the controller
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/checklist/" + checklistItem.getId())
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checked").value(true));

    // 8. Verify changes were saved
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/checklist")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].checked").value(true));

    // 9. Toggle back to unchecked
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/checklist/" + checklistItem.getId())
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checked").value(false));
  }
} 