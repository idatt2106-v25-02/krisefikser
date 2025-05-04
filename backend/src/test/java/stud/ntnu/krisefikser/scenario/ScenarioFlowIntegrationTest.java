package stud.ntnu.krisefikser.scenario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.common.AbstractIntegrationTest;
import stud.ntnu.krisefikser.scenario.dto.CreateScenarioRequest;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class ScenarioFlowIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TurnstileService turnstileService;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setUp() throws Exception {
                // Mock Turnstile verification to always return true for the test token
                when(turnstileService.verify(any())).thenReturn(true);

                // Set up regular user
                setUpUser();

                // Set up admin user
                setUpAdminUser();
        }

        @Test
        void completeScenarioFlow() throws Exception {
                // 1. Create a scenario using admin authentication
                CreateScenarioRequest createRequest = new CreateScenarioRequest();
                createRequest.setTitle("Test Scenario");
                createRequest.setContent(
                                "# Test Scenario Content\n\nThis is a test scenario for emergency preparedness.");

                MvcResult createResult = mockMvc.perform(
                                withAdminAuth(
                                                post("/api/scenarios")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper
                                                                                .writeValueAsString(createRequest))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("Test Scenario"))
                                .andReturn();

                String responseContent = createResult.getResponse().getContentAsString();
                UUID scenarioId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

                // 2. Get all scenarios (can use regular user authentication)
                mockMvc.perform(
                                withJwtAuth(
                                                get("/api/scenarios")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(scenarioId.toString()))
                                .andExpect(jsonPath("$[0].title").value("Test Scenario"));

                // 3. Get specific scenario by ID (can use regular user authentication)
                mockMvc.perform(
                                withJwtAuth(
                                                get("/api/scenarios/" + scenarioId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(scenarioId.toString()))
                                .andExpect(jsonPath("$.title").value("Test Scenario"))
                                .andExpect(jsonPath("$.content")
                                                .value("# Test Scenario Content\n\nThis is a test scenario for emergency preparedness."));

                // 4. Update the scenario (requires admin authentication)
                CreateScenarioRequest updateRequest = new CreateScenarioRequest();
                updateRequest.setTitle("Updated Test Scenario");
                updateRequest.setContent("# Updated Content\n\nThis scenario has been updated.");

                mockMvc.perform(
                                withAdminAuth(
                                                put("/api/scenarios/" + scenarioId)
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper
                                                                                .writeValueAsString(updateRequest))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated Test Scenario"))
                                .andExpect(jsonPath("$.content")
                                                .value("# Updated Content\n\nThis scenario has been updated."));

                // 5. Delete the scenario (requires admin authentication)
                mockMvc.perform(
                                withAdminAuth(
                                                delete("/api/scenarios/" + scenarioId)))
                                .andExpect(status().isNoContent());

                // 6. Verify scenario was deleted (can use regular user authentication)
                mockMvc.perform(
                                withJwtAuth(
                                                get("/api/scenarios/" + scenarioId)))
                                .andExpect(status().isNotFound());

                // 7. Verify scenarios list is now empty (can use regular user authentication)
                MvcResult listResult = mockMvc.perform(
                                withJwtAuth(
                                                get("/api/scenarios")))
                                .andExpect(status().isOk())
                                .andReturn();

                String listContent = listResult.getResponse().getContentAsString();
                assertEquals("[]", listContent);
        }

        @Test
        @WithMockUser
        void regularUserCanReadButNotWrite() throws Exception {
                // First create a scenario as admin
                CreateScenarioRequest createRequest = new CreateScenarioRequest();
                createRequest.setTitle("Admin Created Scenario");
                createRequest.setContent("# Admin Content\n\nOnly admins should be able to modify this.");

                // Create the scenario with admin privileges
                MvcResult createResult = mockMvc.perform(
                                withAdminAuth(
                                                post("/api/scenarios")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper
                                                                                .writeValueAsString(createRequest))))
                                .andExpect(status().isCreated())
                                .andReturn();

                String responseContent = createResult.getResponse().getContentAsString();
                UUID scenarioId = UUID.fromString(objectMapper.readTree(responseContent).get("id").asText());

                // Regular user should be able to read
                mockMvc.perform(get("/api/scenarios/" + scenarioId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Admin Created Scenario"));

                // Regular user should not be able to update
                CreateScenarioRequest updateRequest = new CreateScenarioRequest();
                updateRequest.setTitle("Attempted Update");
                updateRequest.setContent("# Regular User Update\n\nThis should fail.");

                mockMvc.perform(
                                put("/api/scenarios/" + scenarioId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isForbidden());

                // Regular user should not be able to delete
                mockMvc.perform(delete("/api/scenarios/" + scenarioId))
                                .andExpect(status().isForbidden());
        }
}