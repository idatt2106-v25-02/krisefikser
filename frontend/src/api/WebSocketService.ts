import { Client } from '@stomp/stompjs'

export class WebSocketService {
  private client: Client
  private subscriptions: string[]

  constructor(subscriptions: string[] = [], serverUrl: string = 'ws://localhost:8080/ws',  debug: boolean = true) {
    this.client = new Client({
      brokerURL: serverUrl,
      debug: debug ? (msg: string) => console.log(msg) : () => {},
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    })
    this.subscriptions = subscriptions

    this.client.onStompError = function (frame) {
      console.log('Broker reported error: ' + frame.headers['message'])
      console.log('Additional details: ' + frame.body)
    }
  }

  public connect<T>(callback: (message: T) => void) {
    this.client.onConnect = () => {
      this.subscriptions.forEach((subscription) => {
        this.client.subscribe(subscription, (message) => {
          callback(JSON.parse(message.body) as T)
        })
      })
    }
    this.client.activate()
  }
}
