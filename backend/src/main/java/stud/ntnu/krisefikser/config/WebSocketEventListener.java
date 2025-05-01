package stud.ntnu.krisefikser.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    log.info("Received a new web socket connection");
  }
}
