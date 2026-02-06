package com.aptech.controller;

import com.aptech.dto.RoomPresenceDTO;
import com.aptech.dto.UserDTO;
import com.aptech.dto.WebSocketMessage;
import com.aptech.service.PresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * WebSocket controller handling all presence and room management endpoints
 * No business logic - delegates to service layer
 * Uses Lombok for dependency injection and logging
 * Uses validation annotations for input validation
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    /**
     * Endpoint 1: JOIN ROOM
     * Registers a user and adds them to a room
     * 
     * @param message        WebSocket message containing user and room info
     *                       (validated)
     * @param headerAccessor Session header accessor
     */
    @MessageMapping("/join")
    public void joinRoom(@Valid @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        log.info("📥 JOIN request - User: {}, Room: {}, Session: {}",
                message.getUsername(), message.getRoomId(), sessionId);

        try {
            presenceService.handleJoinRoom(
                    sessionId,
                    message.getUserId(),
                    message.getUsername(),
                    message.getRoomId());
            log.info("✅ User {} successfully joined room {}",
                    message.getUsername(), message.getRoomId());
        } catch (Exception e) {
            log.error("❌ Error handling JOIN for user {}: {}",
                    message.getUsername(), e.getMessage());
        }
    }

    /**
     * Endpoint 2: LEAVE ROOM
     * Removes a user from their current room
     * 
     * @param message        WebSocket message (validated)
     * @param headerAccessor Session header accessor
     */
    @MessageMapping("/leave")
    public void leaveRoom(@Valid @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        log.info("📤 LEAVE request - Session: {}", sessionId);

        try {
            presenceService.handleLeaveRoom(sessionId);
            log.info("✅ User left room successfully");
        } catch (Exception e) {
            log.error("❌ Error handling LEAVE: {}", e.getMessage());
        }
    }

    /**
     * Endpoint 3: HEARTBEAT / PING
     * Updates user's last-seen status
     * 
     * @param message        WebSocket message (validated)
     * @param headerAccessor Session header accessor
     */
    @MessageMapping("/ping")
    public void ping(@Valid @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        log.debug("💓 PING received - Session: {}", sessionId);

        try {
            presenceService.handlePing(sessionId);
        } catch (Exception e) {
            log.error("❌ Error handling PING: {}", e.getMessage());
        }
    }

    /**
     * Endpoint 4: GET ROOM PRESENCE
     * Returns a list of users in a specific room
     * Uses mapper to convert entities to DTOs
     * 
     * @param message WebSocket message containing room ID (validated)
     * @return WebSocket message with room presence data
     */
    @MessageMapping("/room/presence")
    @SendToUser("/queue/room-presence")
    public WebSocketMessage getRoomPresence(@Valid @Payload WebSocketMessage message) {
        log.info("👥 ROOM_PRESENCE request for room: {}", message.getRoomId());

        try {
            RoomPresenceDTO roomPresence = presenceService.getRoomPresence(message.getRoomId());

            log.info("✅ Room presence retrieved: {} users in room {}",
                    roomPresence.getUserCount(), message.getRoomId());

            return WebSocketMessage.roomPresenceResponse(message.getRoomId(), roomPresence);
        } catch (Exception e) {
            log.error("❌ Error getting room presence: {}", e.getMessage());
            return WebSocketMessage.roomPresenceResponse(
                    message.getRoomId(),
                    RoomPresenceDTO.builder()
                            .roomId(message.getRoomId())
                            .userCount(0)
                            .build());
        }
    }

    /**
     * Endpoint 5: GET ONLINE USERS
     * Returns a list of all online users
     * Uses mapper to convert entities to DTOs
     * 
     * @param message WebSocket message (validated)
     * @return WebSocket message with online users list
     */
    @MessageMapping("/users/online")
    @SendToUser("/queue/online-users")
    public WebSocketMessage getOnlineUsers(@Valid @Payload WebSocketMessage message) {
        log.info("🌐 ONLINE_USERS request");

        try {
            List<UserDTO> onlineUsers = presenceService.getOnlineUsers();

            log.info("✅ Online users retrieved: {} users", onlineUsers.size());

            return WebSocketMessage.onlineUsersResponse(onlineUsers);
        } catch (Exception e) {
            log.error("❌ Error getting online users: {}", e.getMessage());
            return WebSocketMessage.onlineUsersResponse(List.of());
        }
    }

    /**
     * Additional endpoint: GET SYSTEM STATS
     * Returns system statistics
     * 
     * @param message WebSocket message
     * @return WebSocket message with system stats
     */
    @MessageMapping("/system/stats")
    @SendToUser("/queue/system-stats")
    public WebSocketMessage getSystemStats(@Valid @Payload WebSocketMessage message) {
        log.info("📊 SYSTEM_STATS request");

        try {
            var stats = presenceService.getSystemStats();
            log.info("✅ System stats retrieved: {}", stats);
            return WebSocketMessage.builder()
                    .type(message.getType())
                    .data(stats)
                    .build();
        } catch (Exception e) {
            log.error("❌ Error getting system stats: {}", e.getMessage());
            return WebSocketMessage.builder()
                    .type(message.getType())
                    .data(null)
                    .build();
        }
    }
}
