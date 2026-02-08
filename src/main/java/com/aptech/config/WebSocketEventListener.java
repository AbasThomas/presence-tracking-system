package com.aptech.config;

import com.aptech.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket event listener that reacts to connect and disconnect lifecycle events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("[CONNECT] WebSocket connection established. Session ID: {}", sessionId);
        log.debug("Connection details - User: {}, Attributes: {}",
                headerAccessor.getUser(),
                headerAccessor.getSessionAttributes());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("[DISCONNECT] WebSocket connection closed. Session ID: {}", sessionId);
        log.debug("Disconnect details - Close status: {}", event.getCloseStatus());

        try {
            presenceService.handleDisconnect(sessionId);
            log.info("[DISCONNECT] Successfully cleaned up session: {}", sessionId);
        } catch (Exception e) {
            log.error("[DISCONNECT] Error handling disconnect for session {}: {}", sessionId, e.getMessage());
        }
    }
}
