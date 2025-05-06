export interface BackendNotification {
  id: string; // UUID maps to string
  title: string;
  message: string;
  type: 'INVITE' | 'EVENT' | 'INFO' | 'EXPIRY_REMINDER'; // From NotificationType enum
  url?: string;
  read: boolean;
  createdAt: string; // LocalDateTime maps to string (ISO format)
} 