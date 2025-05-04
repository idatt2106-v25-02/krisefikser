package stud.ntnu.krisefikser.reflection;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.reflection.dto.CreateReflectionRequest;
import stud.ntnu.krisefikser.reflection.dto.UpdateReflectionRequest;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;
import stud.ntnu.krisefikser.reflection.repository.ReflectionRepository;

class ReflectionFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReflectionRepository reflectionRepository;

    @MockBean
    private TurnstileService turnstileService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock Turnstile verification to always return true for the test token
        when(turnstileService.verify(any())).thenReturn(true);
        setUpUser();

        // Clean up any existing reflections
        reflectionRepository.deleteAll();
    }

    @Test
    void createReflection_whenValidInput_shouldCreateAndReturnReflection() throws Exception {
        // Arrange
        CreateReflectionRequest createRequest = TestDataFactory.createTestReflectionRequest(
                "Test Reflection Title",
                "This is the content of a test reflection.",
                VisibilityType.PUBLIC,
                null);

        // Act & Assert
        MvcResult createResult = mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Reflection Title"))
                .andExpect(jsonPath("$.content").value("This is the content of a test reflection."))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.authorName").isNotEmpty()) // Should contain the test user's name
                .andReturn();

        // Extract ID for further tests
        String responseContent = createResult.getResponse().getContentAsString();
        UUID reflectionId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

        // Test getting the created reflection
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/" + reflectionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reflectionId.toString()))
                .andExpect(jsonPath("$.title").value("Test Reflection Title"));
    }

    @Test
    void createPrivateReflection_thenUpdateAndDelete_shouldWorkCorrectly() throws Exception {
        // Arrange - Create a private reflection
        CreateReflectionRequest createRequest = TestDataFactory.createTestReflectionRequest(
                "Private Reflection",
                "This is a private reflection content.",
                VisibilityType.PRIVATE,
                null);

        // Act & Assert - Create reflection
        MvcResult createResult = mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andReturn();

        // Extract ID
        String responseContent = createResult.getResponse().getContentAsString();
        UUID reflectionId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

        // Update the reflection
        UpdateReflectionRequest updateRequest = TestDataFactory.createTestUpdateReflectionRequest(
                "Updated Private Reflection",
                "This is an updated private reflection content.",
                VisibilityType.PRIVATE,
                null);

        mockMvc.perform(
                withJwtAuth(
                        put("/api/reflections/" + reflectionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Private Reflection"))
                .andExpect(jsonPath("$.content").value("This is an updated private reflection content."));

        // Delete the reflection
        mockMvc.perform(
                withJwtAuth(
                        delete("/api/reflections/" + reflectionId)))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/" + reflectionId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMultipleReflections_thenGetAccessibleReflections_shouldReturnAllCreatedReflections() throws Exception {
        // Create a public reflection
        CreateReflectionRequest publicRequest = TestDataFactory.createTestReflectionRequest(
                "Public Reflection",
                "This is a public reflection content.",
                VisibilityType.PUBLIC,
                null);

        mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(publicRequest))))
                .andExpect(status().isCreated());

        // Create a private reflection
        CreateReflectionRequest privateRequest = TestDataFactory.createTestReflectionRequest(
                "Private Reflection",
                "This is a private reflection content.",
                VisibilityType.PRIVATE,
                null);

        mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(privateRequest))))
                .andExpect(status().isCreated());

        // Create a household reflection
        CreateReflectionRequest householdRequest = TestDataFactory.createTestReflectionRequest(
                "Household Reflection",
                "This is a household reflection content.",
                VisibilityType.HOUSEHOLD,
                getTestHousehold().getId());

        mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(householdRequest))))
                .andExpect(status().isCreated());

        // Get all accessible reflections
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // Get only public reflections
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/public")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Public Reflection"));

        // Get only household reflections
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/household")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Household Reflection"));

        // Get only current user reflections
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/my")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void createReflection_withHouseholdVisibility_shouldSetHouseholdDetails() throws Exception {
        // Arrange
        CreateReflectionRequest householdRequest = TestDataFactory.createTestReflectionRequest(
                "Household Reflection",
                "This is a household reflection content.",
                VisibilityType.HOUSEHOLD,
                getTestHousehold().getId());

        // Act & Assert
        mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(householdRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("HOUSEHOLD"))
                .andExpect(jsonPath("$.householdId").value(getTestHousehold().getId().toString()))
                .andExpect(jsonPath("$.householdName").value(getTestHousehold().getName()));
    }

    @Test
    void updateReflection_fromPublicToPrivate_shouldUpdateVisibility() throws Exception {
        // Arrange - Create a public reflection
        CreateReflectionRequest createRequest = TestDataFactory.createTestReflectionRequest(
                "Public Reflection",
                "This is a public reflection content.",
                VisibilityType.PUBLIC,
                null);

        MvcResult createResult = mockMvc.perform(
                withJwtAuth(
                        post("/api/reflections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract ID
        String responseContent = createResult.getResponse().getContentAsString();
        UUID reflectionId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

        // Update to private
        UpdateReflectionRequest updateRequest = TestDataFactory.createTestUpdateReflectionRequest(
                "Now Private Reflection",
                "This reflection is now private.",
                VisibilityType.PRIVATE,
                null);

        // Act & Assert
        mockMvc.perform(
                withJwtAuth(
                        put("/api/reflections/" + reflectionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.title").value("Now Private Reflection"));

        // Confirm it appears in my reflections but not in public
        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/my")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + reflectionId + "')].visibility").value("PRIVATE"));

        mockMvc.perform(
                withJwtAuth(
                        get("/api/reflections/public")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + reflectionId + "')]").isEmpty());
    }
}