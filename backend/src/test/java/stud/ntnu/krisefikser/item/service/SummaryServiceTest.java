package stud.ntnu.krisefikser.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import stud.ntnu.krisefikser.household.dto.HouseholdMemberResponse;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.item.dto.ChecklistItemResponse;
import stud.ntnu.krisefikser.item.dto.FoodItemResponse;
import stud.ntnu.krisefikser.item.dto.InventorySummaryResponse;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private FoodItemService foodItemService;

    @Mock
    private ChecklistItemService checklistItemService;

    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private SummaryService summaryService;

    private Household testHousehold;
    private HouseholdResponse householdResponse;
    private List<HouseholdMemberResponse> householdMembers;
    private UserResponse ownerResponse;
    private List<FoodItemResponse> foodItems;
    private List<ChecklistItemResponse> checklistItems;

    @BeforeEach
    void setUp() {
        // Create test household
        testHousehold = Household.builder()
            .id(UUID.randomUUID())
            .name("Test Household")
            .waterLiters(15.0)
            .build();
            
        // Create user responses for members
        UserResponse user1 = new UserResponse();
        user1.setId(UUID.randomUUID());
        user1.setEmail("user1@example.com");
        user1.setFirstName("User");
        user1.setLastName("One");
        
        UserResponse user2 = new UserResponse();
        user2.setId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        
        // Create owner response
        ownerResponse = new UserResponse();
        ownerResponse.setId(UUID.randomUUID());
        ownerResponse.setEmail("owner@example.com");
        ownerResponse.setFirstName("Owner");
        ownerResponse.setLastName("User");
        
        // Create household members
        HouseholdMemberResponse member1 = new HouseholdMemberResponse();
        member1.setUser(user1);
        member1.setStatus(HouseholdMemberStatus.ACCEPTED);
        
        HouseholdMemberResponse member2 = new HouseholdMemberResponse();
        member2.setUser(user2);
        member2.setStatus(HouseholdMemberStatus.ACCEPTED);
        
        householdMembers = Arrays.asList(member1, member2);
        
        // Create household response
        householdResponse = new HouseholdResponse();
        householdResponse.setId(testHousehold.getId());
        householdResponse.setName(testHousehold.getName());
        householdResponse.setOwner(ownerResponse);
        householdResponse.setMembers(householdMembers);
        householdResponse.setAddress("Test Address");
        householdResponse.setLatitude(0.0);
        householdResponse.setLongitude(0.0);
        householdResponse.setCreatedAt(LocalDateTime.now());
        householdResponse.setActive(true);
        
        // Create food items with total kcal of 5000
        foodItems = Arrays.asList(
            new FoodItemResponse(UUID.randomUUID(), "Item 1", "food_icon", 2000, Instant.now().plusSeconds(86400)),
            new FoodItemResponse(UUID.randomUUID(), "Item 2", "food_icon", 3000, Instant.now().plusSeconds(86400))
        );
        
        // Create checklist items with 2 checked out of 3
        checklistItems = Arrays.asList(
            new ChecklistItemResponse(UUID.randomUUID(), "Item 1", "icon1", true),
            new ChecklistItemResponse(UUID.randomUUID(), "Item 2", "icon2", true),
            new ChecklistItemResponse(UUID.randomUUID(), "Item 3", "icon3", false)
        );
    }

    @Test
    void getInventorySummary_WithData_ShouldReturnCorrectSummary() {
        // Arrange
        when(householdService.getActiveHousehold()).thenReturn(testHousehold);
        when(householdService.toHouseholdResponse(testHousehold)).thenReturn(householdResponse);
        when(foodItemService.getAllFoodItems()).thenReturn(foodItems);
        when(checklistItemService.getAllChecklistItems()).thenReturn(checklistItems);
        
        // Constants used in SummaryService
        final int DAYS_GOAL = 7;
        final int DAILY_KCAL = 2250;
        final int DAILY_WATER_LITERS = 3;
        
        // Calculate expected values
        int expectedKcal = 5000; // Sum of all food items kcal
        int expectedKcalGoal = DAILY_KCAL * DAYS_GOAL * householdMembers.size(); // 2250 * 7 * 2 = 31500
        double expectedWaterLiters = 15.0; // From household
        double expectedWaterLitersGoal = DAILY_WATER_LITERS * DAYS_GOAL * householdMembers.size(); // 3 * 7 * 2 = 42
        int expectedCheckedItems = 2; // Number of checked items
        int expectedTotalItems = 3; // Total number of checklist items
        
        // Act
        InventorySummaryResponse result = summaryService.getInventorySummary();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getKcal()).isEqualTo(expectedKcal);
        assertThat(result.getKcalGoal()).isEqualTo(expectedKcalGoal);
        assertThat(result.getWaterLiters()).isEqualTo(expectedWaterLiters);
        assertThat(result.getWaterLitersGoal()).isEqualTo(expectedWaterLitersGoal);
        assertThat(result.getCheckedItems()).isEqualTo(expectedCheckedItems);
        assertThat(result.getTotalItems()).isEqualTo(expectedTotalItems);
        
        // Verify method calls
        verify(householdService, times(3)).getActiveHousehold();
        verify(householdService, times(2)).toHouseholdResponse(testHousehold);
        verify(foodItemService).getAllFoodItems();
        verify(checklistItemService, times(2)).getAllChecklistItems();
    }
    
    @Test
    void getInventorySummary_WithEmptyData_ShouldReturnZeroValues() {
        // Arrange
        when(householdService.getActiveHousehold()).thenReturn(testHousehold);
        when(householdService.toHouseholdResponse(testHousehold)).thenReturn(householdResponse);
        when(foodItemService.getAllFoodItems()).thenReturn(Collections.emptyList());
        when(checklistItemService.getAllChecklistItems()).thenReturn(Collections.emptyList());
        
        // Set water to zero
        testHousehold.setWaterLiters(0.0);
        
        // Act
        InventorySummaryResponse result = summaryService.getInventorySummary();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getKcal()).isEqualTo(0);
        assertThat(result.getWaterLiters()).isEqualTo(0.0);
        assertThat(result.getCheckedItems()).isEqualTo(0);
        assertThat(result.getTotalItems()).isEqualTo(0);
        
        // Verify method calls
        verify(householdService, times(3)).getActiveHousehold();
        verify(householdService, times(2)).toHouseholdResponse(testHousehold);
        verify(foodItemService).getAllFoodItems();
        verify(checklistItemService, times(2)).getAllChecklistItems();
    }
} 