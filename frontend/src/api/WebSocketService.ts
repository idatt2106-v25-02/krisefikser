import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import type { Frame, Message } from '@stomp/stompjs'

export interface SubscriptionConfig {
  destination: string
  callback: (message: Message) => void
  headers?: Record<string, string>
}

export class WebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, { id: string; callback: (message: Message) => void }> =
    new Map()
  private connectionPromise: Promise<void> | null = null
  private connected = false
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 2000

  constructor(
    private serverUrl: string,
    private connectHeaders: Record<string, string> = {},
    private debug: boolean = false,
  ) {}

  /**
   * Initialize the STOMP client
   */
  public init(): void {
    if (this.client) {
      console.log('WebSocket client already initialized.')
      return
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(this.serverUrl),
      connectHeaders: this.connectHeaders,
      debug: this.debug ? (msg: string) => console.log(msg) : () => {},
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    })

    this.client.onConnect = this.onConnect.bind(this)
    this.client.onStompError = this.onStompError.bind(this)
    this.client.onWebSocketClose = this.onWebSocketClose.bind(this)
  }

  /**
   * Connect to the WebSocket server
   * @returns Promise that resolves when connected
   */
  public connect(): Promise<void> {
    if (!this.client) {
      this.init()
    }

    if (this.connected) {
      return Promise.resolve()
    }

    if (this.connectionPromise) {
      return this.connectionPromise
    }

    this.connectionPromise = new Promise<void>((resolve, reject) => {
      if (!this.client) {
        reject(new Error('STOMP client is not initialized'))
        return
      }

      this.client.onConnect = (frame: Frame) => {
        this.connected = true
        this.reconnectAttempts = 0
        this.onConnect(frame)
        resolve()
      }

      this.client.onStompError = (frame: Frame) => {
        this.onStompError(frame)
        reject(new Error(`STOMP Error: ${frame.headers.message}`))
      }

      try {
        console.log('FUCK YOU')
        this.client.activate()
        console.log("FUCK YOU TOO")
      } catch (error) {
        reject(error)
      }
    })

    return this.connectionPromise
  }

  /**
   * Subscribe to a destination
   * @param config Subscription configuration
   * @returns The subscription ID
   */
  public async subscribe(config: SubscriptionConfig): Promise<string> {
    await this.connect()

    if (!this.client || !this.connected) {
      throw new Error('Not connected to WebSocket server')
    }

    const subscription = this.client.subscribe(
      config.destination,
      (message: Message) => {
        config.callback(message)
      },
      config.headers,
    )

    this.subscriptions.set(config.destination, {
      id: subscription.id,
      callback: config.callback,
    })

    console.log(`Subscribed to ${config.destination} with ID ${subscription.id}`)

    return subscription.id
  }

  /**
   * Unsubscribe from a destination
   * @param destination The destination to unsubscribe from
   */
  public unsubscribe(destination: string): void {
    if (!this.client || !this.connected) {
      console.warn('Not connected to WebSocket server')
      return
    }

    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      this.client.unsubscribe(subscription.id)
      this.subscriptions.delete(destination)
      console.log(`Unsubscribed from ${destination}`)
    } else {
      console.warn(`No subscription found for ${destination}`)
    }
  }

  /**
   * Send a message to a destination
   * @param destination The destination to send to
   * @param body The message body
   * @param headers Optional headers
   */
  public async send(
    destination: string,
    body: string | object,
    headers: Record<string, string> = {},
  ): Promise<void> {
    await this.connect()

    if (!this.client || !this.connected) {
      throw new Error('Not connected to WebSocket server')
    }

    const bodyString = typeof body === 'string' ? body : JSON.stringify(body)

    this.client.publish({
      destination,
      headers,
      body: bodyString,
    })

    console.log(`Sent message to ${destination}`, body)
  }

  /**
   * Disconnect from the WebSocket server
   */
  public disconnect(): Promise<void> {
    return new Promise<void>((resolve) => {
      if (!this.client || !this.connected) {
        this.connected = false
        this.connectionPromise = null
        resolve()
        return
      }

      this.client.deactivate()
      this.connected = false
      this.connectionPromise = null
      this.subscriptions.clear()
      console.log('Disconnected from WebSocket server')
      resolve()
    })
  }

  /**
   * Reconnect to the WebSocket server
   */
  private async reconnect(): Promise<void> {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnect attempts reached')
      return
    }

    this.reconnectAttempts++
      console.log(
        `Reconnecting (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})...`,
      )

    try {
      // Store existing subscriptions to resubscribe after reconnect
      const existingSubscriptions = Array.from(this.subscriptions.entries()).map(
        ([destination, { callback }]) => ({
          destination,
          callback,
        }),
      )

      // Reset state
      this.client = null
      this.connected = false
      this.connectionPromise = null
      this.subscriptions.clear()

      // Reconnect
      await this.connect()

      // Resubscribe to previous subscriptions
      for (const config of existingSubscriptions) {
        await this.subscribe(config)
      }
    } catch (error) {
      console.error('Error reconnecting:', error)

      // Try again after delay
      setTimeout(() => this.reconnect(), this.reconnectDelay)
    }
  }

  /**
   * Handle successful connection
   */
  private onConnect(frame: Frame): void {
    this.connected = true
    console.log('Connected to WebSocket server', frame)
  }

  /**
   * Handle STOMP errors
   */
  private onStompError(frame: Frame): void {
    console.error('STOMP error:', frame)
  }

  /**
   * Handle WebSocket close
   */
  private onWebSocketClose(): void {
    console.log('WebSocket connection closed')
    this.connected = false
    this.connectionPromise = null

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      setTimeout(() => this.reconnect(), this.reconnectDelay)
    }
  }

  /**
   * Check if connected to the WebSocket server
   */
  public isConnected(): boolean {
    return this.connected
  }
}
