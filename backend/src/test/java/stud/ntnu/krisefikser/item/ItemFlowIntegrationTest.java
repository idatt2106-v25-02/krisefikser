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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistCategory;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ItemFlowIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FoodItemRepository foodItemRepository;

  @Autowired
  private ChecklistItemRepository checklistItemRepository;

  @MockBean
  private TurnstileService turnstileService;

  @BeforeEach
  void setUp() throws Exception {
    // Mock Turnstile verification to always return true for the test token
    when(turnstileService.verify(any())).thenReturn(true);
    setUpUser();

    foodItemRepository.deleteAll();
    checklistItemRepository.deleteAll();
  }

  @Test
  void createFoodItem_whenValidInput_shouldCreateAndReturnItem() throws Exception {
    // Arrange
    CreateFoodItemRequest createRequest = TestDataFactory.createTestFoodItemRequest(
        "Test Food Item",
        100,
        Instant.now().plusSeconds(86400) // 1 day from now
    );

    // Act & Assert
    MvcResult createResult = mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            ))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Test Food Item"))
        .andReturn();

    // Extract ID for potential future use
    String responseContent = createResult.getResponse().getContentAsString();
    UUID foodItemId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());
  }

  @Test
  void getAllFoodItems_afterCreatingItem_shouldReturnItemsList() throws Exception {
    // Arrange - Create a food item first
    CreateFoodItemRequest createRequest = TestDataFactory.createTestFoodItemRequest(
        "Test Food Item for List",
        100,
        Instant.now().plusSeconds(86400)
    );

    // Create the item
    MvcResult createResult = mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            ))
        .andReturn();

    String responseContent = createResult.getResponse().getContentAsString();
    UUID foodItemId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

    // Act & Assert - Get all items
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/food")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(foodItemId.toString()))
        .andExpect(jsonPath("$[0].name").value("Test Food Item for List"));
  }

  @Test
  void createFoodItem_withPastExpirationDate_shouldStillCreateItem() throws Exception {
    // Arrange
    CreateFoodItemRequest pastDateRequest = TestDataFactory.createTestFoodItemRequest(
        "Past Date Item",
        200,
        Instant.now().minusSeconds(86400) // 1 day ago
    );

    // Act & Assert
    mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pastDateRequest))
            ))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Past Date Item"));
  }

  @Test
  void createFoodItem_withFarFutureExpirationDate_shouldCreateItem() throws Exception {
    // Arrange
    CreateFoodItemRequest farFutureRequest = TestDataFactory.createTestFoodItemRequest(
        "Far Future Item",
        300,
        Instant.now().plusSeconds(86400 * 365 * 10) // 10 years from now
    );

    // Act & Assert
    mockMvc.perform(
            withJwtAuth(
                post("/api/items/food")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(farFutureRequest))
            ))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Far Future Item"));
  }

  @Test
  void getAllChecklistItems_shouldReturnItems() throws Exception {
    // Arrange - Create a checklist item
    ChecklistItem checklistItem = TestDataFactory.createTestChecklistItem(
        "Test Checklist Item",
        ChecklistCategory.HEALTH_HYGIENE,
        false,
        getTestHousehold()
    );

    checklistItemRepository.save(checklistItem);

    // Act & Assert
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/checklist")
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Test Checklist Item"))
        .andExpect(jsonPath("$[0].checked").value(false));
  }

  @Test
  void toggleChecklistItem_shouldChangeCheckStatus() throws Exception {
    // Arrange - Create an unchecked checklist item
    ChecklistItem checklistItem = TestDataFactory.createTestChecklistItem(
        "Toggle Test Item",
        ChecklistCategory.HEALTH_HYGIENE,
        false,
        getTestHousehold()
    );

    checklistItem = checklistItemRepository.save(checklistItem);

    // Act & Assert - Toggle to checked
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/checklist/" + checklistItem.getId())
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checked").value(true));

    // Act & Assert - Toggle back to unchecked
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/checklist/" + checklistItem.getId())
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checked").value(false));
  }
} 