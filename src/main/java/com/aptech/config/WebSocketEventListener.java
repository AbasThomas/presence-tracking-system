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
 * WebSocket event listener
 * Handles connection and disconnection events
 * Using Lombok for dependency injection and logging
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    /**
     * Handle new WebSocket connection
     * 
     * @param event Session connected event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("🔗 New WebSocket connection established. Session ID: {}", sessionId);
        log.debug("Connection details - User: {}, Remote Address: {}",
                headerAccessor.getUser(),
                headerAccessor.getSessionAttributes());
    }

    /**
     * Handle WebSocket disconnection
     * Automatically cleans up user data
     * 
     * @param event Session disconnect event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("🔌 WebSocket connection closed. Session ID: {}", sessionId);
        log.debug("Disconnect details - Close status: {}", event.getCloseStatus());

        try {
            // Handle graceful disconnect
            presenceService.handleDisconnect(sessionId);
            log.info("✅ Successfully cleaned up session: {}", sessionId);
        } catch (Exception e) {
            log.error("❌ Error handling disconnect for session {}: {}", sessionId, e.getMessage());
        }
    }
}
