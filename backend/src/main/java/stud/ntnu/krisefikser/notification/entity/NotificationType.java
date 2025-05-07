package stud.ntnu.krisefikser.notification.entity;

/**
 * Enumeration representing different types of notifications in the system.
 * Defines the categories or purposes of notifications that can be sent to users.
 *
 * @since 1.0
 */
public enum NotificationType {
  /**
   * Represents an invitation notification, typically used for households.
   */
  INVITE,

  /**
   * Represents an event-related notification.
   */
  EVENT,
  /**
   * General information.
   */
  INFO,
  /**
   * Represents an item expiry reminder notification.
   */
  EXPIRY_REMINDER
}