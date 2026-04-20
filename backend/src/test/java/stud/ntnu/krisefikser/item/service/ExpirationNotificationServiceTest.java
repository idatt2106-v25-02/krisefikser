package stud.ntnu.krisefikser.item.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.service.HouseholdMemberService;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;
import stud.ntnu.krisefikser.notification.service.NotificationService;
import stud.ntnu.krisefikser.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ExpirationNotificationServiceTest {

  @Mock
  private FoodItemRepository foodItemRepository;
  @Mock
  private HouseholdMemberService householdMemberService;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private ExpirationNotificationService expirationNotificationService;

  @Test
  void checkForExpiredItemsAndNotifyUsers_shouldCreateNotificationForOptedInMembers() {
    Household household = Household.builder().id(UUID.randomUUID()).name("Hjem").build();
    FoodItem item = FoodItem.builder()
        .id(UUID.randomUUID())
        .name("Hermetikk")
        .expirationDate(Instant.now().plusSeconds(3600))
        .household(household)
        .build();
    User user = User.builder().email("test@example.com").notifications(true).build();
    HouseholdMember member = HouseholdMember.builder().household(household).user(user).build();

    when(foodItemRepository.findAllByExpirationDateBetween(any(), any())).thenReturn(List.of(item));
    when(householdMemberService.getMembers(household.getId())).thenReturn(List.of(member));

    expirationNotificationService.checkForExpiredItemsAndNotifyUsers();

    verify(notificationService).createNotification(any());
  }

  @Test
  void checkForExpiredItemsAndNotifyUsers_shouldSkipWhenNoItems() {
    when(foodItemRepository.findAllByExpirationDateBetween(any(), any())).thenReturn(List.of());

    expirationNotificationService.checkForExpiredItemsAndNotifyUsers();

    verify(householdMemberService, never()).getMembers(any());
    verify(notificationService, never()).createNotification(any());
  }
}
