import { Client, type IMessage, type StompSubscription, type StompHeaders } from '@stomp/stompjs';

export class WebSocketService {
  private client: Client;
  private activeSubscriptions: Map<string, StompSubscription> = new Map();
  private isConnected: boolean = false;
  private connectionPromise: Promise<void> | null = null;
  private pendingOperations: Array<() => void> = [];

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

    this.connectionPromise = new Promise<void>((resolve) => {
      this.client.onConnect = () => {
        this.isConnected = true;
        console.log('WebSocket connected');

        // Process any operations that were waiting for connection
        this.processPendingOperations();
        resolve();
      };
    });

    this.client.onStompError = (frame) => {
      console.error('Broker reported error:', frame.headers['message']);
      console.error('Additional details:', frame.body);
    };

    this.client.onDisconnect = () => {
      this.isConnected = false;
      this.activeSubscriptions.clear();
      console.log('WebSocket disconnected');

      // Reset connection promise for future reconnection
      this.connectionPromise = new Promise<void>((resolve) => {
        this.client.onConnect = () => {
          this.isConnected = true;
          console.log('WebSocket reconnected');
          this.processPendingOperations();
          resolve();
        };
      });
    };

    this.client.activate();
  }

  /**
   * Process any operations that were queued while waiting for connection
   */
  private processPendingOperations(): void {
    const operations = [...this.pendingOperations];
    this.pendingOperations = [];

    for (const operation of operations) {
      operation();
    }
  }

  /**
   * Returns a promise that resolves when the connection is established
   */
  public waitForConnection(): Promise<void> {
    if (this.isConnected) {
      return Promise.resolve();
    }
    return this.connectionPromise || Promise.reject('Connection attempt not initiated');
  }

  /**
   * Type-safe wrapper for subscribing to topics
   * @param topic The topic to subscribe to
   * @param callback Callback function with typed payload
   * @param options Optional subscription options including headers
   * @returns Promise resolving to Subscription object
   */
  public async subscribe<T>(
    topic: string,
    callback: (payload: T) => void,
    options?: { headers?: StompHeaders }
  ): Promise<StompSubscription> {
    await this.waitForConnection();

    // Unsubscribe first if already subscribed
    await this.unsubscribe(topic);

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
   * Queue a subscription to be processed when connection is available
   * @param topic The topic to subscribe to
   * @param callback Callback function with typed payload
   * @param options Optional subscription options including headers
   * @returns Promise resolving to Subscription object
   */
  public queueSubscription<T>(
    topic: string,
    callback: (payload: T) => void,
    options?: { headers?: StompHeaders }
  ): Promise<StompSubscription> {
    return new Promise((resolve) => {
      const operation = async () => {
        const subscription = await this.subscribe(topic, callback, options);
        resolve(subscription);
      };

      if (this.isConnected) {
        operation();
      } else {
        this.pendingOperations.push(operation);
      }
    });
  }

  /**
   * Unsubscribe from a specific topic
   * @param topic The topic to unsubscribe from
   * @returns Promise that resolves when unsubscription is complete
   */
  public async unsubscribe(topic: string): Promise<void> {
    if (!this.isConnected) {
      // If not connected, just remove from our tracking
      this.activeSubscriptions.delete(topic);
      return;
    }

    const subscription = this.activeSubscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.activeSubscriptions.delete(topic);
    }
  }

  /**
   * Send a message to a destination
   * @param destination The destination to send to
   * @param body The message body
   * @param headers Optional headers
   * @returns Promise that resolves when message is sent
   */
  public async send(destination: string, body: string | object, headers?: StompHeaders): Promise<void> {
    await this.waitForConnection();

    const messageBody = typeof body === 'string' ? body : JSON.stringify(body);
    this.client.publish({
      destination,
      body: messageBody,
      headers
    });
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
   * Check if the client is currently connected
   */
  public isWebSocketConnected(): boolean {
    return this.isConnected;
  }

  /**
   * Disconnect from the WebSocket
   */
  public disconnect(): void {
    this.pendingOperations = [];
    this.unsubscribeAll();
    this.client.deactivate();
  }
}
