package stud.ntnu.krisefikser.household.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.household.dto.CreateGuestRequest;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.dto.GuestResponse;
import stud.ntnu.krisefikser.household.dto.HouseholdMemberResponse;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.dto.JoinHouseholdRequest;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@WebMvcTest(HouseholdController.class)
@Import(TestSecurityConfig.class)
class HouseholdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HouseholdService householdService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private UUID householdId;
    private UUID userId;
    private UUID guestId;
    private HouseholdResponse testHouseholdResponse;
    private CreateHouseholdRequest createHouseholdRequest;
    private JoinHouseholdRequest joinHouseholdRequest;
    private CreateGuestRequest createGuestRequest;
    private GuestResponse guestResponse;
    private UserResponse ownerResponse;
    private List<HouseholdResponse> householdResponses;

    @BeforeEach
    void setUp() {
        householdId = UUID.randomUUID();
        userId = UUID.randomUUID();
        guestId = UUID.randomUUID();

        ownerResponse = new UserResponse(
                userId,
                "owner@example.com",
                Collections.singletonList("USER"),
                "Owner",
                "User",
                false,
                false,
                false,
                null,
                null);

        HouseholdMemberResponse memberResponse = new HouseholdMemberResponse(
                ownerResponse);

        guestResponse = GuestResponse.builder()
                .id(guestId)
                .name("Test Guest")
                .icon("person")
                .consumptionMultiplier(1.0)
                .build();

        testHouseholdResponse = HouseholdResponse.builder()
                .id(householdId)
                .name("Test Household")
                .latitude(63.4305)
                .longitude(10.3951)
                .address("Test Address")
                .owner(ownerResponse)
                .members(Collections.singletonList(memberResponse))
                .guests(Collections.singletonList(guestResponse))
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        createHouseholdRequest = CreateHouseholdRequest.builder()
                .name("New Household")
                .address("New Address")
                .city("New City")
                .postalCode("54321")
                .latitude(63.4305)
                .longitude(10.3951)
                .build();

        createGuestRequest = CreateGuestRequest.builder()
                .name("New Guest")
                .icon("person")
                .consumptionMultiplier(1.2)
                .build();

        joinHouseholdRequest = new JoinHouseholdRequest(householdId);

        householdResponses = Collections.singletonList(testHouseholdResponse);
    }

    @Test
    @WithMockUser
    void getAllHouseholds_ShouldReturnUserHouseholds() throws Exception {
        // Arrange
        when(householdService.getUserHouseholds()).thenReturn(householdResponses);

        // Act & Assert
        mockMvc.perform(get("/api/households/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(householdId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Household"))
                .andExpect(jsonPath("$[0].address").value("Test Address"))
                .andExpect(jsonPath("$[0].guests[0].id").value(guestId.toString()))
                .andExpect(jsonPath("$[0].guests[0].name").value("Test Guest"));
    }

    @Test
    @WithMockUser
    void joinHousehold_ShouldJoinAndReturnHousehold() throws Exception {
        // Arrange
        when(householdService.joinHousehold(householdId)).thenReturn(testHouseholdResponse);

        // Act & Assert
        mockMvc.perform(post("/api/households/join")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinHouseholdRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(householdId.toString()))
                .andExpect(jsonPath("$.name").value("Test Household"));
    }

    @Test
    @WithMockUser
    void setActiveHousehold_ShouldSetActiveAndReturnHousehold() throws Exception {
        // Arrange
        when(householdService.setActiveHousehold(householdId)).thenReturn(testHouseholdResponse);

        // Act & Assert
        mockMvc.perform(post("/api/households/active")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinHouseholdRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(householdId.toString()))
                .andExpect(jsonPath("$.name").value("Test Household"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser
    void getActiveHousehold_ShouldReturnActiveHousehold() throws Exception {

        when(householdService.getActiveHousehold()).thenReturn(null); // The actual entity is not used
        when(householdService.toHouseholdResponse(any())).thenReturn(testHouseholdResponse);

        // Act & Assert
        mockMvc.perform(get("/api/households/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(householdId.toString()))
                .andExpect(jsonPath("$.name").value("Test Household"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.guests[0].id").value(guestId.toString()))
                .andExpect(jsonPath("$.guests[0].name").value("Test Guest"));
    }

    @Test
    @WithMockUser
    void leaveHousehold_ShouldLeaveHousehold() throws Exception {
        // Arrange
        doNothing().when(householdService).leaveHousehold(householdId);

        // Act & Assert
        mockMvc.perform(post("/api/households/leave")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinHouseholdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteHousehold_ShouldDeleteHousehold() throws Exception {
        // Arrange
        doNothing().when(householdService).deleteHousehold(householdId);

        // Act & Assert
        mockMvc.perform(delete("/api/households/{id}", householdId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createHousehold_ShouldCreateAndReturnHousehold() throws Exception {
        // Arrange
        when(householdService.createHousehold(any(CreateHouseholdRequest.class)))
                .thenReturn(testHouseholdResponse);

        // Act & Assert
        mockMvc.perform(post("/api/households")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createHouseholdRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(householdId.toString()))
                .andExpect(jsonPath("$.name").value("Test Household"));
    }

    @Test
    @WithMockUser
    void addGuestToHousehold_ShouldAddGuestAndReturnHousehold() throws Exception {
        // Arrange
        when(householdService.addGuestToHousehold(any(CreateGuestRequest.class)))
                .thenReturn(testHouseholdResponse);

        // Act & Assert
        mockMvc.perform(post("/api/households/guests")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGuestRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(householdId.toString()))
                .andExpect(jsonPath("$.guests[0].id").value(guestId.toString()))
                .andExpect(jsonPath("$.guests[0].name").value("Test Guest"));
    }

    @Test
    @WithMockUser
    void removeGuestFromHousehold_ShouldRemoveGuestAndReturnHousehold() throws Exception {
        // Arrange
        when(householdService.removeGuestFromHousehold(guestId))
                .thenReturn(testHouseholdResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/households/guests/{guestId}", guestId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(householdId.toString()));
    }
}