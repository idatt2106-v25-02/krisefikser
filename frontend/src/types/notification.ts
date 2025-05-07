export interface BackendNotification {
  id: string; // UUID maps to string
  title: string;
  message: string;
  type: 'INVITE' | 'EVENT' | 'INFO' | 'EXPIRY_REMINDER'; // From NotificationType enum
  url?: string;
  read: boolean;
  createdAt: string | number[]; // Allow string OR number array for createdAt
} 