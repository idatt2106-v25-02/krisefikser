package stud.ntnu.krisefikser.item.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.entity.HouseholdMember;
import stud.ntnu.krisefikser.household.service.HouseholdMemberService;
import stud.ntnu.krisefikser.item.entity.FoodItem;
import stud.ntnu.krisefikser.item.repository.FoodItemRepository;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.notification.entity.NotificationType;
import stud.ntnu.krisefikser.notification.service.NotificationService;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Service responsible for periodically checking for expiring food items and
 * notifying users.
 *
 * <p>
 * This service contains a scheduled task that runs daily to identify food items
 * in households that are nearing their expiration date. For each such item, it
 * notifies
 * members of the household, provided their notification preferences are
 * enabled.
 * </p>
 *
 * @see FoodItemRepository
 * @see HouseholdMemberService
 * @see NotificationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpirationNotificationService {

  private static final DateTimeFormatter NORWEGIAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
      .withZone(ZoneId.systemDefault());
  private final FoodItemRepository foodItemRepository;
  private final HouseholdMemberService householdMemberService;
  private final NotificationService notificationService;

  /**
   * Periodically checks for food items nearing their expiration date and sends
   * notifications.
   *
   * <p>
   * This method is scheduled. It queries for {@link FoodItem} entities that will
   * expire
   * within the next 7 days. For each expiring item, it retrieves the members of
   * the associated
   * household. If a household member has notifications enabled
   * ({@link User#isNotifications()}),
   * a notification of type {@link NotificationType#EXPIRY_REMINDER} is created
   * and sent via the
   * {@link NotificationService}.
   * </p>
   *
   * <p>
   * Logs are generated for the process, including the number of items found,
   * notifications sent, and any errors encountered during notification dispatch.
   * </p>
   */
  @Scheduled(fixedRate = 10000)
  public void checkForExpiredItemsAndNotifyUsers() {
    log.info("Starting daily check for expiring food items...");
    Instant now = Instant.now();
    Instant sevenDaysFromNow = now.plus(7, ChronoUnit.DAYS);

    List<FoodItem> expiringItems = foodItemRepository.findAllByExpirationDateBetween(now, sevenDaysFromNow);

    if (expiringItems.isEmpty()) {
      log.info("No food items expiring within the next 7 days.");
      return;
    }

    log.info("Found {} food items expiring soon.", expiringItems.size());

    for (FoodItem item : expiringItems) {
      if (item.getHousehold() == null) {
        log.warn("Food item with ID {} has no associated household. Skipping.", item.getId());
        continue;
      }

      List<HouseholdMember> members = householdMemberService.getMembers(item.getHousehold().getId());
      if (members.isEmpty()) {
        log.warn(
            "Household with ID {} for item '{}' has no members. Skipping.",
            item.getHousehold().getId(),
            item.getName());
        continue;
      }

      for (HouseholdMember member : members) {
        User user = member.getUser();
        if (user.isNotifications()) {
          try {
            Instant expiryInstant = item.getExpirationDate();
            String userFriendlyExpirationDate = NORWEGIAN_DATE_FORMATTER.format(expiryInstant);

            Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.EXPIRY_REMINDER)
                .title("Vare utløper snart!")
                .message(
                    String.format(
                        "%s utløper den %s.",
                        item.getName(),
                        userFriendlyExpirationDate))
                .isRead(false)
                .item(item)
                .build();
            notificationService.createNotification(notification);
            log.info(
                "Sent expiry notification for item '{}' to user '{}'",
                item.getName(),
                user.getEmail());
          } catch (Exception e) {
            log.error(
                "Failed to send expiry notification for item '{}' to user '{}': {}",
                item.getName(),
                user.getEmail(),
                e.getMessage(),
                e);
          }
        } else {
          log.info(
              "User '{}' has notifications disabled. Skipping expiry alert for item '{}'.",
              user.getEmail(),
              item.getName());
        }
      }
    }
    log.info("Finished daily check for expiring food items.");
  }
}