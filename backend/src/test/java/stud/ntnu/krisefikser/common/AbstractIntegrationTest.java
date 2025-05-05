package stud.ntnu.krisefikser.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

        private static final String TEST_USER_EMAIL = "brotherman@testern.no";
        private static final String ADMIN_USER_EMAIL = "admin@testern.no";
        private static final String DEFAULT_PASSWORD = "password";
        private static final String TEST_HOUSEHOLD_NAME = "Test Household";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private HouseholdRepository householdRepository;

        @MockitoBean
        private TurnstileService turnstileService;

        private String accessToken;

        @Getter
        private User testUser;

        @Getter
        private User adminUser;

        @Getter
        private Household testHousehold;

        @Autowired
        private DatabaseCleanupService databaseCleanupService;

        public MockHttpServletRequestBuilder withJwtAuth(MockHttpServletRequestBuilder requestBuilder) {
                return requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        /**
         * Creates a request builder with admin authentication using Spring Security's
         * test support.
         * This is more reliable than using JWT tokens in tests.
         *
         * @param requestBuilder The request builder to enhance with admin
         *                       authentication
         * @return The enhanced request builder
         */
        public MockHttpServletRequestBuilder withAdminAuth(MockHttpServletRequestBuilder requestBuilder) {
                return requestBuilder.with(SecurityMockMvcRequestPostProcessors.user(ADMIN_USER_EMAIL)
                                .password(DEFAULT_PASSWORD)
                                .roles("ADMIN"));
        }

        /**
         * Creates a request builder with user authentication using Spring Security's
         * test support.
         * This is more reliable than using JWT tokens in tests.
         *
         * @param requestBuilder The request builder to enhance with user authentication
         * @return The enhanced request builder
         */
        public MockHttpServletRequestBuilder withUserAuth(MockHttpServletRequestBuilder requestBuilder) {
                return requestBuilder.with(SecurityMockMvcRequestPostProcessors.user(TEST_USER_EMAIL)
                                .password(DEFAULT_PASSWORD)
                                .roles("USER"));
        }

        @Transactional
        public void setUpUser() throws Exception {
                try {
                        log.info("Setting up test user...");

                        // Delete previous test data
                        databaseCleanupService.clearDatabase();

                        // Make turnstileService.verify returns true
                        Mockito.when(turnstileService.verify(ArgumentMatchers.anyString())).thenReturn(true);

                        // Create User
                        RegisterRequest request = new RegisterRequest(
                                        TEST_USER_EMAIL,
                                        DEFAULT_PASSWORD,
                                        "Test",
                                        "User",
                                        "turnstile-token");

                        String responseContent = mockMvc.perform(
                                        post("/api/auth/register")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString();

                        // Parse response and extract token
                        Map<String, String> responseMap = objectMapper.readValue(responseContent,
                                        new TypeReference<>() {
                                        });

                        this.accessToken = responseMap.get("accessToken");
                        log.debug("Got access token: {}", accessToken);

                        // Fetch the user from the database
                        this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
                                        .orElseThrow(() -> new RuntimeException("Test user not found"));

                        // Create Household - using both JWT and Spring Security for maximum
                        // compatibility
                        createHousehold();

                        // Re-fetch user to ensure we have the latest state including household
                        this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
                                        .orElseThrow(() -> new RuntimeException("Test user not found"));

                        // Ensure testHousehold is properly set
                        this.testHousehold = testUser.getActiveHousehold();

                        // Double check the household is properly initialized
                        if (this.testHousehold == null) {
                                throw new RuntimeException("Test household is null after setup");
                        }

                        // Make sure waterLiters are initialized (important for
                        // InventorySummaryIntegrationTest)
                        if (this.testHousehold.getWaterLiters() == null) {
                                this.testHousehold.setWaterLiters(100.0);
                                householdRepository.save(this.testHousehold);

                                // Re-fetch after update
                                this.testHousehold = householdRepository.findById(this.testHousehold.getId())
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Could not find household after update"));
                        }

                        log.info("Test user setup complete. User: {}, Household: {} (waterLiters: {})",
                                        testUser.getEmail(),
                                        testHousehold.getName(),
                                        testHousehold.getWaterLiters());
                } catch (Exception e) {
                        log.error("Failed to set up test user", e);
                        throw e;
                }
        }

        /**
         * Sets up an admin user for testing administrative operations.
         * This creates a real admin user in the database that can be used for tests.
         * 
         * @throws Exception if setup fails
         */
        @Transactional
        public void setUpAdminUser() throws Exception {
                // Create admin user if not exists
                if (adminUser == null) {
                        // Create User
                        RegisterRequest request = new RegisterRequest(
                                        ADMIN_USER_EMAIL,
                                        DEFAULT_PASSWORD,
                                        "Admin",
                                        "User",
                                        "turnstile-token");

                        mockMvc.perform(
                                        post("/api/auth/register")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andReturn();

                        // Fetch the admin user from database
                        this.adminUser = userRepository.findByEmail(ADMIN_USER_EMAIL)
                                        .orElseThrow(() -> new RuntimeException("Admin user not found"));

                        // Add admin role
                        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                                        .orElseThrow(() -> new RuntimeException("Admin role not found"));
                        adminUser.getRoles().add(adminRole);
                        userRepository.save(adminUser);

                        log.info("Admin user created: {}", adminUser.getEmail());
                }
        }

        public Household getTestHousehold() {
                if (testHousehold != null) {
                        return testHousehold;
                }
                return getTestUser().getActiveHousehold();
        }

        private Map<String, String> getPostResponse(String uri, String body) {
                try {
                        String responseContent = mockMvc.perform(
                                        post(uri)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString();

                        // Parse response and extract token
                        return objectMapper.readValue(responseContent,
                                        new TypeReference<>() {
                                        });
                } catch (Exception e) {
                        log.error("Failed to parse response", e);
                        throw new RuntimeException("Failed to parse response", e);
                }
        }

        private void createHousehold() {
                CreateHouseholdRequest createHouseholdRequest = CreateHouseholdRequest.builder()
                                .name(TEST_HOUSEHOLD_NAME)
                                .latitude(0.0)
                                .longitude(0.0)
                                .address("Test Address")
                                .city("Test City")
                                .postalCode("12345")
                                .build();

                try {
                        log.debug("Creating household: {}", TEST_HOUSEHOLD_NAME);

                        // Try with JWT auth first
                        MvcResult result = mockMvc.perform(
                                        withJwtAuth(
                                                        post("/api/households")
                                                                        .contentType(MediaType.APPLICATION_JSON)
                                                                        .content(objectMapper.writeValueAsString(
                                                                                        createHouseholdRequest))))
                                        .andReturn();

                        int status = result.getResponse().getStatus();
                        log.debug("Create household status with JWT: {}", status);

                        // If JWT auth failed, try with Spring Security mock auth
                        if (status != 201) {
                                log.warn("JWT auth failed with status {}, trying with Spring Security mock auth",
                                                status);
                                mockMvc.perform(
                                                withUserAuth(
                                                                post("/api/households")
                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                .content(objectMapper
                                                                                                .writeValueAsString(
                                                                                                                createHouseholdRequest))))
                                                .andExpect(status().isCreated())
                                                .andReturn();
                        }

                        // Re-fetch user to ensure we have the latest state including household
                        this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
                                        .orElseThrow(() -> new RuntimeException("Test user not found"));

                        // Set testHousehold directly
                        this.testHousehold = testUser.getActiveHousehold();

                        if (this.testHousehold == null) {
                                log.warn("Active household not set on user, trying to find households");

                                // If not set via user, try to find household by iterating through all
                                // households
                                Optional<Household> optionalHousehold = householdRepository.findAll().stream()
                                                .filter(h -> TEST_HOUSEHOLD_NAME.equals(h.getName()))
                                                .findFirst();

                                if (optionalHousehold.isPresent()) {
                                        this.testHousehold = optionalHousehold.get();
                                } else {
                                        throw new RuntimeException("Failed to create and find household");
                                }

                                // Connect user to household if not already connected
                                if (testUser.getActiveHousehold() == null) {
                                        testUser.setActiveHousehold(this.testHousehold);
                                        userRepository.save(testUser);
                                }
                        }

                        // Ensure water liters is set
                        if (this.testHousehold.getWaterLiters() == null) {
                                this.testHousehold.setWaterLiters(100.0);
                                householdRepository.save(this.testHousehold);
                        }

                        log.debug("Created household: {} with ID: {}", testHousehold.getName(), testHousehold.getId());
                } catch (Exception e) {
                        log.error("Failed to create household", e);
                        throw new RuntimeException("Failed to create household", e);
                }
        }
}
