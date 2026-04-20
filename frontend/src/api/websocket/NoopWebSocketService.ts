import type { StompHeaders, StompSubscription } from '@stomp/stompjs'
import type { IWebSocketService } from './IWebSocketService'

const noopSubscription: StompSubscription = {
  id: 'noop-subscription',
  unsubscribe: () => {},
}

export class NoopWebSocketService implements IWebSocketService {
  public waitForConnection(): Promise<void> {
    return Promise.resolve()
  }

  public subscribe<T>(
    _topic: string,
    _callback: (payload: T) => void,
    _options?: { headers?: StompHeaders },
  ): Promise<StompSubscription> {
    return Promise.resolve(noopSubscription)
  }

  public queueSubscription<T>(
    _topic: string,
    _callback: (payload: T) => void,
    _options?: { headers?: StompHeaders },
  ): Promise<StompSubscription> {
    return Promise.resolve(noopSubscription)
  }

  public unsubscribe(_topic: string): Promise<void> {
    return Promise.resolve()
  }

  public send(_destination: string, _body: string | object, _headers?: StompHeaders): Promise<void> {
    return Promise.resolve()
  }

  public unsubscribeAll(): void {}

  public isWebSocketConnected(): boolean {
    return false
  }

  public disconnect(): void {}
}
