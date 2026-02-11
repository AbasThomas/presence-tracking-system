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
 * WebSocket controller handling all presence and room management endpoints.
 * No business logic lives here; everything is delegated to the service layer.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    /**
     * Endpoint 1: JOIN ROOM
     * Registers a user and adds them to a room.
     */
    @MessageMapping("/join")
    public void joinRoom(@Valid @Payload WebSocketMessage message,
                         SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("[JOIN] request - User: {}, Room: {}, Session: {}",
                message.getUsername(), message.getRoomId(), sessionId);

        try {
            presenceService.handleJoinRoom(
                    sessionId,
                    message.getUserId(),
                    message.getUsername(),
                    message.getRoomId());
            log.info("[JOIN] User {} successfully joined room {}",
                    message.getUsername(), message.getRoomId());
        } catch (Exception e) {
            log.error("[JOIN] Error handling JOIN for user {}: {}",
                    message.getUsername(), e.getMessage());
        }
    }

    /**
     * Endpoint 2: LEAVE ROOM
     * Removes a user from their current room.
     */
    @MessageMapping("/leave")
    public void leaveRoom(@Valid @Payload WebSocketMessage message,
                          SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("[LEAVE] request - Session: {}", sessionId);

        try {
            presenceService.handleLeaveRoom(sessionId);
            log.info("[LEAVE] User left room successfully");
        } catch (Exception e) {
            log.error("[LEAVE] Error handling LEAVE: {}", e.getMessage());
        }
    }

    /**
     * Endpoint 3: HEARTBEAT / PING
     * Updates the user's last-seen status.
     */
    @MessageMapping("/ping")
    public void ping(@Valid @Payload WebSocketMessage message,
                     SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("[PING] received - Session: {}", sessionId);

        try {
            presenceService.handlePing(sessionId);
        } catch (Exception e) {
            log.error("[PING] Error handling PING: {}", e.getMessage());
        }
    }

    /**
     * Endpoint 4: GET ROOM PRESENCE
     * Returns a list of users in a specific room.
     */
    @MessageMapping("/room/presence")
    @SendToUser("/queue/room-presence")
    public WebSocketMessage getRoomPresence(@Valid @Payload WebSocketMessage message) {
        log.info("[ROOM_PRESENCE] request for room: {}", message.getRoomId());

        try {
            RoomPresenceDTO roomPresence = presenceService.getRoomPresence(message.getRoomId());
            log.info("[ROOM_PRESENCE] Room presence retrieved: {} users in room {}",
                    roomPresence.getUserCount(), message.getRoomId());
            return WebSocketMessage.roomPresenceResponse(message.getRoomId(), roomPresence);
        } catch (Exception e) {
            log.error("[ROOM_PRESENCE] Error getting room presence: {}", e.getMessage());
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
     * Returns a list of all online users.
     */
    @MessageMapping("/users/online")
    @SendToUser("/queue/online-users")
    public WebSocketMessage getOnlineUsers(@Valid @Payload WebSocketMessage message) {
        log.info("[ONLINE_USERS] request");

        try {
            List<UserDTO> onlineUsers = presenceService.getOnlineUsers();
            log.info("[ONLINE_USERS] Online users retrieved: {} users", onlineUsers.size());
            return WebSocketMessage.onlineUsersResponse(onlineUsers);
        } catch (Exception e) {
            log.error("[ONLINE_USERS] Error getting online users: {}", e.getMessage());
            return WebSocketMessage.onlineUsersResponse(List.of());
        }
    }

    /**
     * Endpoint 6: LIST ROOMS
     */
    @MessageMapping("/rooms/list")
    @SendToUser("/queue/rooms")
    public WebSocketMessage getRooms() {
        log.info("[ROOM_LIST] request");
        return WebSocketMessage.roomListResponse(presenceService.getRoomSummaries());
    }

    /**
     * Endpoint 7: CREATE ROOM
     */
    @MessageMapping("/room/create")
    public void createRoom(@Valid @Payload WebSocketMessage message) {
        log.info("[ROOM_CREATE] request for room {}", message.getRoomId());
        presenceService.createRoom(message.getRoomId());
    }

    /**
     * Endpoint 8: CHAT SEND
     */
    @MessageMapping("/chat/send")
    public void sendChat(@Valid @Payload WebSocketMessage message,
                         SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("[CHAT] message incoming for room {} from {}", message.getRoomId(), message.getUsername());
        presenceService.handleChatMessage(sessionId, message.getRoomId(), message.getContent());
    }

    /**
     * Endpoint 9: CHAT HISTORY
     */
    @MessageMapping("/chat/history")
    @SendToUser("/queue/chat-history")
    public WebSocketMessage getChatHistory(@Valid @Payload WebSocketMessage message) {
        log.info("[CHAT_HISTORY] request for room {}", message.getRoomId());
        return WebSocketMessage.chatHistoryResponse(
                message.getRoomId(),
                presenceService.getChatHistory(message.getRoomId()));
    }

    /**
     * Endpoint 10: CALL SIGNAL
     */
    @MessageMapping("/call/signal")
    public void callSignal(@Valid @Payload WebSocketMessage message,
                           SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("[CALL_SIGNAL] {} -> {} type {}", message.getUsername(), message.getTargetUserId(), message.getSignalType());
        presenceService.handleCallSignal(sessionId, message);
    }

    /**
     * Additional endpoint: GET SYSTEM STATS
     * Returns system statistics.
     */
    @MessageMapping("/system/stats")
    @SendToUser("/queue/system-stats")
    public WebSocketMessage getSystemStats(@Valid @Payload WebSocketMessage message) {
        log.info("[SYSTEM_STATS] request");

        try {
            var stats = presenceService.getSystemStats();
            log.info("[SYSTEM_STATS] System stats retrieved: {}", stats);
            return WebSocketMessage.builder()
                    .type(message.getType())
                    .data(stats)
                    .build();
        } catch (Exception e) {
            log.error("[SYSTEM_STATS] Error getting system stats: {}", e.getMessage());
            return WebSocketMessage.builder()
                    .type(message.getType())
                    .data(null)
                    .build();
        }
    }
}
