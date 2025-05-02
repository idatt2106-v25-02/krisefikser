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
    // 1. Create a food item - using the basic fields available in CreateFoodItemRequest
    CreateFoodItemRequest createRequest = new CreateFoodItemRequest();
    createRequest.setName("Test Food Item");
    createRequest.setIcon("test-icon");
    createRequest.setKcal(100);
    createRequest.setExpirationDate(Instant.now().plusSeconds(86400)); // 1 day from now

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

    // 3. Create checklist items
    ChecklistItem checklistItem = ChecklistItem.builder()
        .name("Test Checklist Item")
        .checked(false)
        .type(ChecklistType.HEALTH)
        .household(getTestHousehold())
        .build();

    checklistItemRepository.save(checklistItem);

    // 4. Get all checklist items for the household
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/checklist")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Test Checklist Item"))
        .andExpect(jsonPath("$[0].checked").value(false));

    // 5. Toggle checklist item status - Use the correct endpoint path as defined in the controller
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/checklist/" + checklistItem.getId())
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checked").value(true));

    // 6. Verify changes were saved
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/checklist")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].checked").value(true));
  }
} 