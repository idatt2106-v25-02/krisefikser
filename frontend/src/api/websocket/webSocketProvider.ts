import { NoopWebSocketService } from './NoopWebSocketService'
import { WebSocketService } from './WebSocketService'
import type { IWebSocketService } from './IWebSocketService'

const isCypressRuntime = (): boolean => {
  return typeof window !== 'undefined' && 'Cypress' in window
}

export const createWebSocketService = (): IWebSocketService => {
  if (isCypressRuntime()) {
    return new NoopWebSocketService()
  }

  return new WebSocketService()
}
