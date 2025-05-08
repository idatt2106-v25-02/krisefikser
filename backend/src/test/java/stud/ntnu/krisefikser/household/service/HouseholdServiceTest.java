package stud.ntnu.krisefikser.household.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdRequest;
import stud.ntnu.krisefikser.household.dto.HouseholdMemberResponse;
import stud.ntnu.krisefikser.household.dto.HouseholdResponse;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.household.repository.GuestRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.item.service.ChecklistItemService;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;
import stud.ntnu.krisefikser.household.repository.HouseholdInviteRepository;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

  @Mock
  private HouseholdRepository householdRepository;

  @Mock
  private HouseholdMemberService householdMemberService;

  @Mock
  private UserService userService;

  @Mock
  private ChecklistItemService checklistItemService;

  @Mock
  private GuestRepository guestRepository;

  @Mock
  private HouseholdInviteRepository inviteRepository;

  @InjectMocks
  private HouseholdService householdService;

  private User testUser;
  private Household testHousehold;
  private HouseholdMember testMember;
  private UUID householdId;
  private UUID userId;
  private CreateHouseholdRequest createHouseholdRequest;
  private List<HouseholdMember> mockMembersList;
  private UserResponse userResponse;
  private HouseholdMemberResponse memberResponse;

  @BeforeEach
  void setUp() {
    // Set up test data
    userId = UUID.randomUUID();
    householdId = UUID.randomUUID();

    Role role = new Role();
    role.setName(Role.RoleType.USER);

    testUser = User.builder()
        .id(userId)
        .email("test@example.com")
        .firstName("Test")
        .lastName("User")
        .roles(Collections.singleton(role))
        .build();

    testHousehold = Household.builder()
        .id(householdId)
        .name("Test Household")
        .owner(testUser)
        .address("Test Address")
        .city("Test City")
        .postalCode("12345")
        .latitude(63.4305)
        .longitude(10.3951)
        .waterLiters(100.0)
        .createdAt(LocalDateTime.now())
        .build();

    testMember = HouseholdMember.builder()
        .user(testUser)
        .household(testHousehold)
        .build();

    createHouseholdRequest = CreateHouseholdRequest.builder()
        .name("New Household")
        .address("New Address")
        .city("New City")
        .postalCode("54321")
        .latitude(63.4305)
        .longitude(10.3951)
        .build();

    mockMembersList = Collections.singletonList(testMember);

    userResponse = new UserResponse(userId, "test@example.com", Collections.emptyList(),
        "Test", "User", false, false, false, null, null);

    memberResponse = new HouseholdMemberResponse(userResponse);

    // Set up mock for GuestRepository using lenient() to avoid
    // UnnecessaryStubbingException
    lenient().when(guestRepository.findByHousehold(any(Household.class))).thenReturn(Collections.emptyList());

    // Set up mock for HouseholdInviteRepository
    lenient().when(inviteRepository.findByHousehold(any(Household.class))).thenReturn(Collections.emptyList());
  }

  @Test
  void getHouseholdById_WhenHouseholdExists_ShouldReturnHousehold() {
    // Arrange
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));

    // Act
    Household result = householdService.getHouseholdById(householdId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(householdId);
    assertThat(result.getName()).isEqualTo(testHousehold.getName());
    verify(householdRepository).findById(householdId);
  }

  @Test
  void getHouseholdById_WhenHouseholdDoesNotExist_ShouldThrowException() {
    // Arrange
    when(householdRepository.findById(householdId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> householdService.getHouseholdById(householdId))
        .isInstanceOf(HouseholdNotFoundException.class)
        .hasMessageContaining("Household not found");
    verify(householdRepository).findById(householdId);
  }

  @Test
  void createHousehold_ShouldCreateAndReturnHousehold() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);

    when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> {
      Household savedHousehold = invocation.getArgument(0);
      savedHousehold.setId(householdId);
      return savedHousehold;
    });

    when(householdMemberService.addMember(any(Household.class), eq(testUser))).thenReturn(
        testMember);
    doNothing().when(userService).updateActiveHousehold(any(Household.class));
    doNothing().when(checklistItemService).createDefaultChecklistItems(any(Household.class));
    when(householdMemberService.getMembers(any())).thenReturn(mockMembersList);

    // Act
    HouseholdResponse result = householdService.createHousehold(createHouseholdRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(createHouseholdRequest.getName());
    assertThat(result.getAddress()).isEqualTo(createHouseholdRequest.getAddress());
    verify(userService, times(2)).getCurrentUser();
    verify(householdRepository).save(any(Household.class));
    verify(householdMemberService).addMember(any(Household.class), eq(testUser));
    verify(userService).updateActiveHousehold(any(Household.class));
    verify(checklistItemService).createDefaultChecklistItems(any(Household.class));
  }

  @Test
  void getUserHouseholds_ShouldReturnUserHouseholds() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdMemberService.getHouseholdsByUser(testUser)).thenReturn(mockMembersList);
    when(householdMemberService.getMembers(any())).thenReturn(mockMembersList);

    // Act
    List<HouseholdResponse> result = householdService.getUserHouseholds();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testHousehold.getId());
    verify(userService, times(2)).getCurrentUser();
    verify(householdMemberService).getHouseholdsByUser(testUser);
  }

  @Test
  void joinHousehold_WhenHouseholdExists_ShouldAddUserAndReturnHousehold() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(false);
    when(householdMemberService.addMember(testHousehold, testUser)).thenReturn(testMember);
    when(householdMemberService.getMembers(householdId)).thenReturn(mockMembersList);

    // Act
    HouseholdResponse result = householdService.joinHousehold(householdId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(householdId);
    verify(userService, times(2)).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(householdMemberService).addMember(testHousehold, testUser);
  }

  @Test
  void joinHousehold_WhenAlreadyMember_ShouldThrowException() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> householdService.joinHousehold(householdId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Already a member of this household");

    verify(userService).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(householdMemberService, never()).addMember(any(), any());
  }

  @Test
  void setActiveHousehold_WhenMemberOfHousehold_ShouldUpdateAndReturnHousehold() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(true);
    doNothing().when(userService).updateActiveHousehold(testHousehold);
    when(householdMemberService.getMembers(householdId)).thenReturn(mockMembersList);

    // Act
    HouseholdResponse result = householdService.setActiveHousehold(householdId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(householdId);
    verify(userService, times(2)).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(userService).updateActiveHousehold(testHousehold);
  }

  @Test
  void setActiveHousehold_WhenNotMember_ShouldThrowException() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> householdService.setActiveHousehold(householdId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Not a member of this household");

    verify(userService).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(userService, never()).updateActiveHousehold(any());
  }

  @Test
  void getActiveHousehold_WhenHouseholdExists_ShouldReturnHousehold() {
    // Arrange
    testUser.setActiveHousehold(testHousehold);
    when(userService.getCurrentUser()).thenReturn(testUser);

    // Act
    Household result = householdService.getActiveHousehold();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(householdId);
    verify(userService).getCurrentUser();
  }

  @Test
  void getActiveHousehold_WhenNoActiveHousehold_ShouldThrowException() {
    // Arrange
    testUser.setActiveHousehold(null);
    when(userService.getCurrentUser()).thenReturn(testUser);

    // Act & Assert
    assertThatThrownBy(() -> householdService.getActiveHousehold())
        .isInstanceOf(HouseholdNotFoundException.class);

    verify(userService).getCurrentUser();
  }

  @Test
  void leaveHousehold_WhenNotMember_ShouldThrowException() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> householdService.leaveHousehold(householdId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Not a member of this household");

    verify(userService).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(userService, never()).updateActiveHousehold(any());
    verify(householdMemberService, never()).removeMember(any(), any());
  }

  @Test
  void deleteHousehold_WhenOwner_ShouldDeleteHousehold() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(householdMemberService.getMembers(householdId)).thenReturn(mockMembersList);
    lenient().doNothing().when(userService).updateActiveHousehold(null);
    doNothing().when(householdMemberService).removeMember(any(), any());
    doNothing().when(householdRepository).deleteById(householdId);

    // Act
    householdService.deleteHousehold(householdId);

    // Assert
    verify(userService).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdMemberService).getMembers(householdId);
    verify(householdRepository).deleteById(householdId);
  }

  @Test
  void deleteHousehold_WhenNotOwner_ShouldThrowException() {
    // Arrange
    User otherUser = User.builder()
        .id(UUID.randomUUID())
        .email("other@example.com")
        .build();

    testHousehold.setOwner(otherUser);

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));

    // Act & Assert
    assertThatThrownBy(() -> householdService.deleteHousehold(householdId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Only the owner can delete a household");

    verify(userService).getCurrentUser();
    verify(householdRepository).findById(householdId);
    verify(householdRepository, never()).deleteById(any());
  }

  @Test
  void addMemberToHousehold_WhenNotAlreadyMember_ShouldAddAndReturnHousehold() {
    // Arrange
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(userService.getUserById(userId)).thenReturn(testUser);
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(false);
    when(householdMemberService.addMember(testHousehold, testUser)).thenReturn(testMember);
    when(householdMemberService.getMembers(householdId)).thenReturn(mockMembersList);
    when(userService.getCurrentUser()).thenReturn(testUser);

    // Act
    HouseholdResponse result = householdService.addMemberToHousehold(householdId, userId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(householdId);
    verify(householdRepository).findById(householdId);
    verify(userService).getUserById(userId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(householdMemberService).addMember(testHousehold, testUser);
  }

  @Test
  void addMemberToHousehold_WhenAlreadyMember_ShouldThrowException() {
    // Arrange
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(userService.getUserById(userId)).thenReturn(testUser);
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> householdService.addMemberToHousehold(householdId, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User is already a member of this household");

    verify(householdRepository).findById(householdId);
    verify(userService).getUserById(userId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(householdMemberService, never()).addMember(any(), any());
  }

  @Test
  void removeMemberFromHousehold_WhenMember_ShouldRemoveAndReturnHousehold() {
    // Arrange
    testUser.setActiveHousehold(testHousehold);

    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(userService.getUserById(userId)).thenReturn(testUser);
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(true);
    doNothing().when(userService).updateActiveHousehold(null);
    doNothing().when(householdMemberService).removeMember(testHousehold, testUser);
    when(householdMemberService.getMembers(householdId)).thenReturn(mockMembersList);
    when(userService.getCurrentUser()).thenReturn(testUser);

    // Act
    HouseholdResponse result = householdService.removeMemberFromHousehold(householdId, userId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(householdId);
    verify(householdRepository).findById(householdId);
    verify(userService).getUserById(userId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(userService).updateActiveHousehold(null);
    verify(householdMemberService).removeMember(testHousehold, testUser);
  }

  @Test
  void removeMemberFromHousehold_WhenNotMember_ShouldThrowException() {
    // Arrange
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(testHousehold));
    when(userService.getUserById(userId)).thenReturn(testUser);
    when(householdMemberService.isMemberOfHousehold(testUser, testHousehold)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> householdService.removeMemberFromHousehold(householdId, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User is not a member of this household");

    verify(householdRepository).findById(householdId);
    verify(userService).getUserById(userId);
    verify(householdMemberService).isMemberOfHousehold(testUser, testHousehold);
    verify(householdMemberService, never()).removeMember(any(), any());
  }

  @Test
  void setWaterAmount_WhenActiveHouseholdExists_ShouldUpdateWaterAmount() {
    // Arrange
    double waterAmount = 150.0;
    testUser.setActiveHousehold(testHousehold);

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(householdRepository.save(testHousehold)).thenReturn(testHousehold);

    // Act
    householdService.setWaterAmount(waterAmount);

    // Assert
    assertThat(testHousehold.getWaterLiters()).isEqualTo(waterAmount);
    verify(userService).getCurrentUser();
    verify(householdRepository).save(testHousehold);
  }

  @Test
  void setWaterAmount_WhenNoActiveHousehold_ShouldThrowException() {
    // Arrange
    double waterAmount = 150.0;
    testUser.setActiveHousehold(null);

    when(userService.getCurrentUser()).thenReturn(testUser);

    // Act & Assert
    assertThatThrownBy(() -> householdService.setWaterAmount(waterAmount))
        .isInstanceOf(HouseholdNotFoundException.class);

    verify(userService).getCurrentUser();
    verify(householdRepository, never()).save(any());
  }
}