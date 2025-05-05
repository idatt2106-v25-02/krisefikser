package stud.ntnu.krisefikser.notification.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Repository interface for Notification entities.
 * Provides CRUD operations and custom query methods for notifications.
 * Extends JpaRepository for basic JPA operations.
 *
 * @since 1.0
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  /**
   * Finds all notifications for a specific user with pagination support.
   * Returns notifications as NotificationResponse DTOs for efficient data transfer.
   *
   * @param user the user whose notifications to retrieve
   * @param pageable pagination information (page number, size, and sorting)
   * @return Page of NotificationResponse objects for the given user
   */
  Page<NotificationResponse> findByUser(User user, Pageable pageable);
}