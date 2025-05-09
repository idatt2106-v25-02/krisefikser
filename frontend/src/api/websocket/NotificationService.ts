// frontend/src/api/websocket/NotificationService.ts
import { useNotificationStore } from '@/stores/notificationStore'
import type { NotificationResponse } from '@/api/generated/model/notificationResponse'
import { webSocket } from '@/main'
import speechService from '@/services/tts/speechService'
import { useAccessibilityStore } from '@/stores/tts/accessibilityStore'

export class NotificationService {
  private static instance: NotificationService
  private isSubscribed = false
  private subscribedEmail: string | null = null
  private notificationStore = useNotificationStore()

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
      await webSocket.subscribe<NotificationResponse>(
        `/user/${userEmail}/queue/notifications`,
        (notification) => {
          // Add notification to store
          this.notificationStore.addNotification(notification)

          // Speak the notification if TTS is enabled
          try {
            const accessibilityStore = useAccessibilityStore()
            if (accessibilityStore.ttsEnabled) {
              const notificationText = `${notification.title}. ${notification.message}`
              speechService.speak(notificationText)
            }
          } catch (error) {
            console.warn('Could not access accessibility store:', error)
          }
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
