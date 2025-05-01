package stud.ntnu.krisefikser.item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.item.dto.CreateFoodItemRequest;
import stud.ntnu.krisefikser.item.entity.ChecklistItem;
import stud.ntnu.krisefikser.item.enums.ChecklistType;
import stud.ntnu.krisefikser.item.repository.ChecklistItemRepository;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ItemFlowIntegrationTest {

  private static final String TEST_USER_EMAIL = "test-items@example.com";
  private static final String DEFAULT_PASSWORD = "password123";
  private static final String TEST_HOUSEHOLD_NAME = "Test Household for Items";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FoodItemRepository foodItemRepository;

  @Autowired
  private ChecklistItemRepository checklistItemRepository;

  @Autowired
  private UserRepository userRepository;

  private String accessToken;
  private User testUser;

  private MockHttpServletRequestBuilder withJwtAuth(MockHttpServletRequestBuilder requestBuilder,
      String token) {
    return requestBuilder.header("Authorization", "Bearer " + token);
  }

  @BeforeEach
  void setUp() throws Exception {
    // Clean up any previous test data
    foodItemRepository.deleteAll();
    checklistItemRepository.deleteAll();

    // Create User
    RegisterRequest request = new RegisterRequest(
        TEST_USER_EMAIL,
        DEFAULT_PASSWORD,
        "Test",
        "User"
    );

    String responseContent = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                    "/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Parse response and extract token
    Map<String, String> responseMap = objectMapper.readValue(responseContent,
        new TypeReference<>() {
        });

    this.accessToken = responseMap.get("accessToken");

    // Create Household
    CreateHouseholdRequest createHouseholdRequest = CreateHouseholdRequest.builder()
        .name(TEST_HOUSEHOLD_NAME)
        .latitude(0.0)
        .longitude(0.0)
        .address("Test Address")
        .city("Test City")
        .postalCode("12345")
        .build();

    mockMvc.perform(
        withJwtAuth(
            post("/api/households")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createHouseholdRequest)),
            accessToken
        )).andReturn();


  }

  private User getTestUser() {
    return testUser = testUser == null ? userRepository.findByEmail(TEST_USER_EMAIL)
        .orElseThrow(() -> new RuntimeException("Test user not found")) : testUser;
  }

  private Household getTestHousehold() {
    return getTestUser().getActiveHousehold();
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
                    .content(objectMapper.writeValueAsString(createRequest)),
                accessToken
            ))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Test Food Item"))
        .andReturn();

    String responseContent = createResult.getResponse().getContentAsString();
    UUID foodItemId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

    // 2. Get all food items for the household
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/food"),
                accessToken
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
                get("/api/items/checklist"),
                accessToken
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Test Checklist Item"))
        .andExpect(jsonPath("$[0].checked").value(false));

    // 5. Toggle checklist item status - Use the correct endpoint path as defined in the controller
    mockMvc.perform(
            withJwtAuth(
                put("/api/items/checklist/" + checklistItem.getId()),
                accessToken
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checked").value(true));

    // 6. Verify changes were saved
    mockMvc.perform(
            withJwtAuth(
                get("/api/items/checklist"),
                accessToken
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].checked").value(true));
  }
} 