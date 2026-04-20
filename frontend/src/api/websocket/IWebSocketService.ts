import type { StompHeaders, StompSubscription } from '@stomp/stompjs'

export interface IWebSocketService {
  waitForConnection(): Promise<void>
  subscribe<T>(
    topic: string,
    callback: (payload: T) => void,
    options?: { headers?: StompHeaders },
  ): Promise<StompSubscription>
  queueSubscription<T>(
    topic: string,
    callback: (payload: T) => void,
    options?: { headers?: StompHeaders },
  ): Promise<StompSubscription>
  unsubscribe(topic: string): Promise<void>
  send(destination: string, body: string | object, headers?: StompHeaders): Promise<void>
  unsubscribeAll(): void
  isWebSocketConnected(): boolean
  disconnect(): void
}
