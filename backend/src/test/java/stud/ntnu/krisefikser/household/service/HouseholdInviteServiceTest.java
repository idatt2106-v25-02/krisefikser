package stud.ntnu.krisefikser.household.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.household.dto.CreateHouseholdInviteRequest;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.repository.HouseholdInviteRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdMemberRepository;
import stud.ntnu.krisefikser.household.repository.HouseholdRepository;
import stud.ntnu.krisefikser.notification.service.NotificationService;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HouseholdInviteServiceTest {

  @Mock
  private HouseholdInviteRepository inviteRepository;
  @Mock
  private HouseholdRepository householdRepository;
  @Mock
  private HouseholdMemberRepository memberRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserService userService;
  @Mock
  private HouseholdService householdService;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private HouseholdInviteService householdInviteService;

  @Test
  void createInvite_shouldFailWhenNoTargetUserOrEmailProvided() {
    CreateHouseholdInviteRequest request = new CreateHouseholdInviteRequest(UUID.randomUUID(), null, null);

    assertThatThrownBy(() -> householdInviteService.createInvite(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Either invitedUserId or invitedEmail");
  }

  @Test
  void createInvite_shouldFailWhenCurrentUserNotHouseholdMember() {
    UUID householdId = UUID.randomUUID();
    User currentUser = User.builder().email("owner@example.com").build();
    Household household = Household.builder().id(householdId).name("Hjem").build();
    CreateHouseholdInviteRequest request = new CreateHouseholdInviteRequest(householdId, null,
        "invite@example.com");

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(memberRepository.existsByUserAndHousehold(currentUser, household)).thenReturn(false);

    assertThatThrownBy(() -> householdInviteService.createInvite(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("member of the household");
  }
}
