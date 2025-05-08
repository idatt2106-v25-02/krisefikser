// frontend/src/api/websocket/NotificationService.ts
import { useNotificationStore } from '@/stores/notificationStore'
import type { NotificationResponse } from '@/api/generated/model/notificationResponse'
import { webSocket } from '@/main.ts'

export class NotificationService {
  private static instance: NotificationService
  private isSubscribed = false
  private subscribedEmail: string | null = null

  private constructor() {}

  public static getInstance(): NotificationService {
    if (!NotificationService.instance) {
      NotificationService.instance = new NotificationService()
    }
    return NotificationService.instance
  }

  /**
   * Subscribe to notifications for the current user
   * This should be called from a Vue component with the user's email
   */
  public async subscribeToNotifications(userEmail: string): Promise<void> {
    // If already subscribed for this email, do nothing
    if (this.isSubscribed && this.subscribedEmail === userEmail) {
      return
    }

    // If subscribed for a different email, unsubscribe first
    if (this.isSubscribed && this.subscribedEmail !== userEmail) {
      await this.unsubscribeFromNotifications()
    }

    if (!userEmail) {
      return
    }

    try {
      // Get a fresh reference to the notification store
      const notificationStore = useNotificationStore()

      await webSocket.subscribe<NotificationResponse>(
        `/user/${userEmail}/queue/notifications`,
        (notification) => {
          console.log('Received notification:', notification)
          notificationStore.addNotification(notification)
        },
      )

      this.isSubscribed = true
      this.subscribedEmail = userEmail
    } catch (error) {
      console.error('Failed to subscribe to notifications:', error)
    }
  }

  public async unsubscribeFromNotifications(): Promise<void> {
    if (!this.isSubscribed || !this.subscribedEmail) {
      return
    }

    try {
      await webSocket.unsubscribe(`/user/${this.subscribedEmail}/queue/notifications`)
      this.isSubscribed = false
      this.subscribedEmail = null
    } catch (error) {
      console.error('Failed to unsubscribe from notifications:', error)
    }
  }
}
