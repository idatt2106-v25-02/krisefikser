package stud.ntnu.krisefikser.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.entity.Household;
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

    private String accessToken;

    @Getter
    private User testUser;

    @Getter
    private User adminUser;

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

    @Transactional
    public void setUpUser() throws Exception {
        // Delete brotherman testern if exists
        databaseCleanupService.clearDatabase();

        // Create User
        RegisterRequest request = new RegisterRequest(
                TEST_USER_EMAIL,
                DEFAULT_PASSWORD,
                "Test",
                "User",
                "turnstile-token");

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

        // Fetch the user from the database
        this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));

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
                                .content(objectMapper.writeValueAsString(createHouseholdRequest))))
                .andReturn();

        // Re-fetch the user with the active household
        this.testUser = userRepository.findByEmail(TEST_USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));

        log.info("Access token: {}", accessToken);
        log.info("Test user: {}", testUser);
        log.info("Test household: {}", getTestHousehold());
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
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                            "/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(
                            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                    .andReturn();

            // Fetch the admin user from database
            this.adminUser = userRepository.findByEmail(ADMIN_USER_EMAIL)
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));

            // Add admin role
            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            adminUser.getRoles().add(adminRole);
            userRepository.save(adminUser);

            log.info("Admin user created: {}", adminUser);
        }
    }

    public Household getTestHousehold() {
        return getTestUser().getActiveHousehold();
    }
}
