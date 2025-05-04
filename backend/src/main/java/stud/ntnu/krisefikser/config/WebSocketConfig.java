package stud.ntnu.krisefikser.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration class for the Krisefikser application.
 *
 * <p>This class configures WebSocket communication using STOMP (Simple Text Oriented Messaging Protocol)
 * as the messaging protocol. It defines connection endpoints and message broker settings that enable
 * real-time communication between the server and clients.</p>
 *
 * <p>The configuration includes:</p>
 * <ul>
 *   <li>A WebSocket endpoint at "/ws" with SockJS fallback support</li>
 *   <li>Message broker configuration with application destination prefix "/app"</li>
 *   <li>Enabled simple broker to relay messages on destinations prefixed with "/topic"</li>
 * </ul>
 *
 * <p>This setup allows:</p>
 * <ul>
 *   <li>Clients to connect to the WebSocket server via "/ws"</li>
 *   <li>Messages sent to "/app/..." to be routed to @MessageMapping methods in controllers</li>
 *   <li>Subscription to channels with the "/topic/..." prefix to receive broadcast messages</li>
 * </ul>
 *
 * @author NTNU Krisefikser Team
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final FrontendConfig frontendConfig;

  /**
   * Registers STOMP endpoints for WebSocket communication.
   *
   * <p>This method configures a single endpoint "/ws" with SockJS fallback support.
   * SockJS provides WebSocket emulation in browsers where WebSockets aren't available,
   * ensuring the application works in all environments.</p>
   *
   * <p>Clients can connect to this endpoint using:</p>
   * <pre>
   * var socket = new SockJS('/ws');
   * var stompClient = Stomp.over(socket);
   * </pre>
   *
   * @param registry The STOMP endpoint registry to configure
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOrigins(frontendConfig.getUrl());
  }

  /**
   * Configures the message broker for WebSocket communication.
   *
   * <p>This method sets up:</p>
   * <ul>
   *   <li>"/app" as the prefix for messages that should be routed to @MessageMapping methods
   *       in annotated controllers</li>
   *   <li>A simple in-memory message broker to deliver messages to clients on destinations
   *       prefixed with "/topic"</li>
   * </ul>
   *
   * <p>With this configuration:</p>
   * <ul>
   *   <li>Client-to-server messages should be sent to destinations starting with "/app/"</li>
   *   <li>Clients can subscribe to destinations starting with "/topic/" to receive broadcast messages</li>
   * </ul>
   *
   * @param registry The message broker registry to configure
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic");
  }
}