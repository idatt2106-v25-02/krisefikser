package stud.ntnu.krisefikser.household.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.enums.HouseholdMemberStatus;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.user.entity.User;

@ExtendWith(MockitoExtension.class)
class HouseholdMemberServiceTest {

  @Mock
  private HouseholdMemberRepository householdMemberRepository;

  @InjectMocks
  private HouseholdMemberService householdMemberService;

  private User testUser;
  private Household testHousehold;
  private HouseholdMember testMember;
  private UUID householdId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    // Set up test data
    userId = UUID.randomUUID();
    householdId = UUID.randomUUID();

    testUser = User.builder()
        .id(userId)
        .email("test@example.com")
        .firstName("Test")
        .lastName("User")
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
        .build();

    testMember = HouseholdMember.builder()
        .user(testUser)
        .household(testHousehold)
        .status(HouseholdMemberStatus.ACCEPTED)
        .build();
  }

  @Test
  void getMembers_ShouldReturnMembersForHousehold() {
    // Arrange
    List<HouseholdMember> expectedMembers = Arrays.asList(testMember);
    when(householdMemberRepository.findByHouseholdId(householdId)).thenReturn(expectedMembers);

    // Act
    List<HouseholdMember> result = householdMemberService.getMembers(householdId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUser()).isEqualTo(testUser);
    assertThat(result.get(0).getHousehold()).isEqualTo(testHousehold);
    verify(householdMemberRepository).findByHouseholdId(householdId);
  }

  @Test
  void isMemberOfHousehold_WhenIsMember_ShouldReturnTrue() {
    // Arrange
    when(householdMemberRepository.existsByUserAndHousehold(testUser, testHousehold)).thenReturn(
        true);

    // Act
    boolean result = householdMemberService.isMemberOfHousehold(testUser, testHousehold);

    // Assert
    assertThat(result).isTrue();
    verify(householdMemberRepository).existsByUserAndHousehold(testUser, testHousehold);
  }

  @Test
  void isMemberOfHousehold_WhenNotMember_ShouldReturnFalse() {
    // Arrange
    when(householdMemberRepository.existsByUserAndHousehold(testUser, testHousehold)).thenReturn(
        false);

    // Act
    boolean result = householdMemberService.isMemberOfHousehold(testUser, testHousehold);

    // Assert
    assertThat(result).isFalse();
    verify(householdMemberRepository).existsByUserAndHousehold(testUser, testHousehold);
  }

  @Test
  void addMember_ShouldCreateAndReturnMembership() {
    // Arrange
    when(householdMemberRepository.save(any(HouseholdMember.class))).thenReturn(testMember);

    // Act
    HouseholdMember result = householdMemberService.addMember(testHousehold, testUser);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(testUser);
    assertThat(result.getHousehold()).isEqualTo(testHousehold);
    assertThat(result.getStatus()).isEqualTo(HouseholdMemberStatus.ACCEPTED);
    verify(householdMemberRepository).save(any(HouseholdMember.class));
  }

  @Test
  void removeMember_WhenMemberExists_ShouldRemoveMembership() {
    // Arrange
    when(householdMemberRepository.findByHouseholdAndUser(testHousehold, testUser))
        .thenReturn(Optional.of(testMember));

    // Act
    householdMemberService.removeMember(testHousehold, testUser);

    // Assert
    verify(householdMemberRepository).findByHouseholdAndUser(testHousehold, testUser);
    verify(householdMemberRepository).delete(testMember);
  }

  @Test
  void removeMember_WhenMemberDoesNotExist_ShouldThrowException() {
    // Arrange
    when(householdMemberRepository.findByHouseholdAndUser(testHousehold, testUser))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> householdMemberService.removeMember(testHousehold, testUser))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Member not found");

    verify(householdMemberRepository).findByHouseholdAndUser(testHousehold, testUser);
  }

  @Test
  void getHouseholdsByUser_ShouldReturnUserMemberships() {
    // Arrange
    List<HouseholdMember> expectedMemberships = Arrays.asList(testMember);
    when(householdMemberRepository.findByUser(testUser)).thenReturn(expectedMemberships);

    // Act
    List<HouseholdMember> result = householdMemberService.getHouseholdsByUser(testUser);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUser()).isEqualTo(testUser);
    assertThat(result.get(0).getHousehold()).isEqualTo(testHousehold);
    verify(householdMemberRepository).findByUser(testUser);
  }
}