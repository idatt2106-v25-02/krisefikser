package stud.ntnu.krisefikser.household.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepo;

    @Mock
    private UserService userService;

    @InjectMocks
    private HouseholdService householdService;

    private User testUser;
    private Household testHousehold;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();
            
        testHousehold = Household.builder()
            .id(UUID.randomUUID())
            .name("Test Household")
            .owner(testUser)
            .waterLiters(100.0)
            .build();
    }

    @Test
    void setWaterAmount_WhenHouseholdExists_ShouldUpdateWaterAmount() {
        // Arrange
        double newWaterAmount = 150.0;
        testUser.setActiveHousehold(testHousehold);
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        
        // Act
        householdService.setWaterAmount(newWaterAmount);
        
        // Assert
        assertThat(testHousehold.getWaterLiters()).isEqualTo(newWaterAmount);
        verify(userService).getCurrentUser();
        verify(householdRepo).save(testHousehold);
    }
    
    @Test
    void setWaterAmount_WhenNoActiveHousehold_ShouldThrowHouseholdNotFoundException() {
        // Arrange
        double newWaterAmount = 150.0;
        testUser.setActiveHousehold(null);
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        
        // Act & Assert
        assertThatThrownBy(() -> householdService.setWaterAmount(newWaterAmount))
            .isInstanceOf(HouseholdNotFoundException.class)
            .hasMessage("Household not found");
            
        verify(userService).getCurrentUser();
        verify(householdRepo, never()).save(any());
    }
} 