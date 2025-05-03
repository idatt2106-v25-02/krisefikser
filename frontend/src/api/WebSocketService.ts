import { Client, type IMessage, type StompSubscription, type StompHeaders } from '@stomp/stompjs';

export class WebSocketService {
  private client: Client;
  private activeSubscriptions: Map<string, StompSubscription> = new Map();
  private isConnected: boolean = false;

  constructor(
    serverUrl: string = 'ws://localhost:8080/ws',
    debug: boolean = true
  ) {
    this.client = new Client({
      brokerURL: serverUrl,
      debug: debug ? (msg: string) => console.log(msg) : () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      this.isConnected = true;
      console.log('WebSocket connected');
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error:', frame.headers['message']);
      console.error('Additional details:', frame.body);
    };

    this.client.onDisconnect = () => {
      this.isConnected = false;
      this.activeSubscriptions.clear();
      console.log('WebSocket disconnected');
    };

    this.client.activate();
  }

  /**
   * Type-safe wrapper for subscribing to topics
   * @param topic The topic to subscribe to
   * @param callback Callback function with typed payload
   * @param options Optional subscription options including headers
   * @returns Subscription object
   */
  public subscribe<T>(
    topic: string,
    callback: (payload: T) => void,
    options?: { headers?: StompHeaders }
  ): StompSubscription {
    if (!this.isConnected) {
      throw new Error('WebSocket is not connected');
    }

    // Unsubscribe first if already subscribed
    this.unsubscribe(topic);

    const subscription = this.client.subscribe(
      topic,
      (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body) as T;
          callback(payload);
        } catch (error) {
          console.error(`Error parsing message from ${topic}:`, error);
        }
      },
      options?.headers
    );

    this.activeSubscriptions.set(topic, subscription);
    return subscription;
  }

  /**
   * Unsubscribe from a specific topic
   * @param topic The topic to unsubscribe from
   */
  public unsubscribe(topic: string): void {
    const subscription = this.activeSubscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.activeSubscriptions.delete(topic);
    }
  }

  /**
   * Unsubscribe from all active subscriptions
   */
  public unsubscribeAll(): void {
    this.activeSubscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.activeSubscriptions.clear();
  }

  /**
   * Disconnect from the WebSocket
   */
  public disconnect(): void {
    this.unsubscribeAll();
    this.client.deactivate();
  }
}
