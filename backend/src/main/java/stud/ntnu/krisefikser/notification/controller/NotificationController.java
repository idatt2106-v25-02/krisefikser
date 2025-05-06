package stud.ntnu.krisefikser.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.notification.service.NotificationService;

/**
 * REST controller for managing notifications within the crisis management system.
 *
 * <p>This controller provides endpoints for retrieving, marking as read, and deleting notifications
 * for the authenticated user. Notifications serve as alerts or informational messages
 * within the system that might require user attention.</p>
 *
 * <p>The controller delegates business logic to the {@link NotificationService}, which handles
 * the actual retrieval and manipulation of notification data.</p>
 *
 * @author Krisefikser Development Team
 * @see NotificationService
 * @see NotificationResponse
 * @since 1.0
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {

  /**
   * Service component responsible for notification operations. Handles retrieval, marking as read,
   * and deletion of notifications for the authenticated user.
   */
  private final NotificationService notificationService;

  /**
   * Retrieves all notifications for the authenticated user.
   *
   * <p>This endpoint returns a paginated list of notifications
   * for the currently authenticated user.
   * The notifications are sorted based on criteria defined in the service layer,
   * typically with newest
   * notifications first.</p>
   *
   * @param pageable pagination information including page number, size, and optional sorting
   * @return ResponseEntity containing a page of user notifications with HTTP status 200 (OK)
   * @see NotificationResponse
   */
  @Operation(summary = "Get all notifications for authenticated user",
      description = "Retrieves a page of the users notifications")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications")
  })
  @GetMapping
  public ResponseEntity<Page<NotificationResponse>> getNotifications(Pageable pageable) {
    return ResponseEntity.ok(notificationService.getNotifications(pageable));
  }

  /**
   * Counts unread notifications for the authenticated user.
   *
   * <p>This endpoint returns the total number of unread notifications for the currently
   * authenticated user. This count can be used by the frontend to display notification badges
   * or indicators.</p>
   *
   * @return ResponseEntity containing the count of unread notifications with HTTP status 200 (OK)
   */
  @Operation(summary = "Get count of unread notifications",
      description = "Returns the number of unread notifications for the authenticated user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Successfully retrieved unread notification count")})
  @GetMapping("/unread")
  public ResponseEntity<Long> getUnreadCount() {
    return ResponseEntity.ok(notificationService.countUnreadNotifications());
  }

  /**
   * Marks a specific notification as read.
   *
   * <p>This endpoint allows users to mark an individual notification as read by providing its
   * unique identifier. Once marked as read, the notification will no longer be counted in the
   * unread notifications total.</p>
   *
   * @param id the unique identifier of the notification to mark as read. Must be a valid UUID.
   * @return ResponseEntity with HTTP status 204 (No Content) indicating successful operation
   * @throws jakarta.persistence.EntityNotFoundException if no notification
   *                                                     with the specified ID exists
   */
  @Operation(summary = "Mark notification as read",
      description = "Marks a specific notification as read by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Notification successfully marked as read"),
      @ApiResponse(responseCode = "404", description = "Notification not found")})
  @PutMapping("/read/{id}")
  public ResponseEntity<Void> readNotification(@PathVariable UUID id) {
    notificationService.markNotificationAsRead(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Marks all notifications for the authenticated user as read.
   *
   * <p>This endpoint allows users to mark all their notifications as read in a single operation.
   * This is useful for bulk management of notifications when the user wishes to clear all
   * notification indicators at once.</p>
   *
   * @return ResponseEntity with HTTP status 204 (No Content) indicating successful operation
   */
  @Operation(summary = "Mark all notifications as read",
      description = "Marks all notifications for the authenticated user as read")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204",
          description = "All notifications successfully marked as read")})
  @PutMapping("/readAll")
  public ResponseEntity<Void> readAll() {
    notificationService.markAllNotificationsAsRead();
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes a specific notification.
   *
   * <p>This endpoint allows users to permanently remove a notification from the system by providing
   * its unique identifier. Once deleted, the notification cannot be retrieved.</p>
   *
   * @param id the unique identifier of the notification to delete. Must be a valid UUID.
   * @return ResponseEntity with HTTP status 204 (No Content) indicating successful deletion
   * @throws jakarta.persistence.EntityNotFoundException if no notification
   *                                                     with the specified ID exists
   */
  @Operation(summary = "Delete notification",
      description = "Deletes a specific notification by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Notification successfully deleted"),
      @ApiResponse(responseCode = "404", description = "Notification not found")})
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
    notificationService.deleteNotification(id);
    return ResponseEntity.noContent().build();
  }
}