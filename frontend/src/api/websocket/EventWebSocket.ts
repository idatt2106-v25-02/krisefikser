// src/services/EventWebSocket.ts
import { Client } from '@stomp/stompjs'
import type { Event } from '@/api/generated/model/event'
import type { Frame, Message } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ref } from 'vue'

class EventWebSocket {
  private client: Client
  private connected = ref(false)
  private connectionAttempts = 0
  private maxReconnectAttempts = 5
  public events = ref<Event[]>([])

  constructor(private baseUrl: string = 'http://localhost:8080') {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${this.baseUrl}/ws`),
      debug: function (str) {
        console.log(str)
      },
    })
    this.setupConnectionHandlers()
  }

  private setupConnectionHandlers(): void {
    this.client.onConnect = (frame: Frame) => {
      console.log('Connected to WebSocket:', frame)
      this.connected.value = true
      this.connectionAttempts = 0
      this.subscribeToTopics()
    }

    this.client.onStompError = (frame: Frame) => {
      console.error('STOMP error:', frame)
      this.handleConnectionError(new Error(`STOMP error: ${frame.headers['message']}`))
    }

    this.client.onWebSocketError = (error: Event) => {
      console.error('WebSocket error:', error)
      this.handleConnectionError(new Error('WebSocket connection error'))
    }
  }

  private handleConnectionError(error: Error): void {
    console.error('Connection error:', error)
    this.connected.value = false

    if (this.connectionAttempts < this.maxReconnectAttempts) {
      this.connectionAttempts++
      console.log(`Reconnection attempt ${this.connectionAttempts} of ${this.maxReconnectAttempts}`)
      setTimeout(() => this.connect(), 3000 * this.connectionAttempts)
    } else {
      console.error('Max reconnection attempts reached.')
    }
  }

  public connect(): void {
    if (!this.client.active) {
      console.log('Connecting to WebSocket...')
      this.client.activate()
    }
  }

  public disconnect(): void {
    if (this.client.active) {
      console.log('Disconnecting from WebSocket...')
      this.client.deactivate()
      this.connected.value = false
    }
  }

  private subscribeToTopics(): void {
    // Subscribe to event updates
    this.client.subscribe('/topic/events', (message: Message) => {
      const event = JSON.parse(message.body) as Event
      console.log('Event updated:', event)
      this.updateEvent(event)
    })

    // Subscribe to new events
    this.client.subscribe('/topic/events/new', (message: Message) => {
      const event = JSON.parse(message.body) as Event
      console.log('New event created:', event)
      this.addEvent(event)
    })

    // Subscribe to event deletions
    this.client.subscribe('/topic/events/delete', (message: Message) => {
      const eventId = JSON.parse(message.body) as number
      console.log('Event deleted:', eventId)
      this.removeEvent(eventId)
    })
  }

  private updateEvent(updatedEvent: Event): void {
    const index = this.events.value.findIndex((e) => e.id === updatedEvent.id)
    if (index !== -1) {
      this.events.value[index] = updatedEvent
      // Create a new array to trigger reactivity
      this.events.value = [...this.events.value]
    }
  }

  private addEvent(newEvent: Event): void {
    this.events.value = [...this.events.value, newEvent]
  }

  private removeEvent(eventId: number): void {
    this.events.value = this.events.value.filter((e) => e.id !== eventId)
  }

  public isConnected() {
    return this.connected
  }
}
export const eventWebSocket = new EventWebSocket()
