package com.aptech.service;

import com.aptech.dto.RoomPresenceDTO;
import com.aptech.dto.UserDTO;
import com.aptech.dto.WebSocketMessage;
import com.aptech.exception.UserNotFoundException;
import com.aptech.mapper.RoomPresenceMapper;
import com.aptech.mapper.UserMapper;
import com.aptech.model.MessageType;
import com.aptech.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service handling presence and room management logic
 * Uses MapStruct mappers for entity-DTO conversion
 * Uses thread-safe collections for concurrent access
 * Uses Lombok for clean code
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;
    private final RoomPresenceMapper roomPresenceMapper;

    // Thread-safe maps for state management
    private final Map<String, User> sessionToUser = new ConcurrentHashMap<>();
    private final Map<String, User> userIdToUser = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomToUsers = new ConcurrentHashMap<>();

    /**
     * Handle user joining a room
     * 
     * @param sessionId WebSocket session ID
     * @param userId    User identifier
     * @param username  User display name
     * @param roomId    Room identifier
     */
    public void handleJoinRoom(String sessionId, String userId, String username, String roomId) {
        log.info("User {} (session: {}) joining room: {}", username, sessionId, roomId);

        // Remove user from previous room if exists
        User existingUser = sessionToUser.get(sessionId);
        if (existingUser != null && existingUser.getCurrentRoom() != null) {
            log.info("User {} is switching from room {} to {}",
                    username, existingUser.getCurrentRoom(), roomId);
            handleLeaveRoom(sessionId);
        }

        // Create or update user using Lombok builder
        User user = User.builder()
                .userId(userId)
                .username(username)
                .sessionId(sessionId)
                .currentRoom(roomId)
                .lastSeen(LocalDateTime.now())
                .online(true)
                .createdAt(existingUser != null ? existingUser.getCreatedAt() : LocalDateTime.now())
                .build();

        sessionToUser.put(sessionId, user);
        userIdToUser.put(userId, user);

        // Add user to room using thread-safe set
        roomToUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        // Broadcast current room presence to all users in the room
        broadcastRoomPresence(roomId);

        log.info("User {} successfully joined room {}. Room now has {} users",
                username, roomId, roomToUsers.get(roomId).size());
    }

    /**
     * Handle user leaving a room
     * 
     * @param sessionId WebSocket session ID
     */
    public void handleLeaveRoom(String sessionId) {
        User user = sessionToUser.get(sessionId);
        if (user == null) {
            log.warn("Attempted to leave room for non-existent session: {}", sessionId);
            throw new UserNotFoundException(sessionId);
        }

        String roomId = user.getCurrentRoom();
        if (roomId == null) {
            log.warn("User {} has no current room", user.getUsername());
            return;
        }

        log.info("User {} leaving room: {}", user.getUsername(), roomId);

        // Remove user from room
        Set<String> roomUsers = roomToUsers.get(roomId);
        if (roomUsers != null) {
            roomUsers.remove(user.getUserId());
            if (roomUsers.isEmpty()) {
                roomToUsers.remove(roomId);
                log.info("Room {} is now empty and removed", roomId);
            }
        }

        // Broadcast updated presence to remaining users
        broadcastRoomPresence(roomId);

        // Update user state using Lombok methods
        user.setCurrentRoom(null);
        user.goOffline();

        log.info("User {} successfully left room {}", user.getUsername(), roomId);
    }

    /**
     * Handle heartbeat/ping to update last seen
     * 
     * @param sessionId WebSocket session ID
     */
    public void handlePing(String sessionId) {
        User user = sessionToUser.get(sessionId);
        if (user != null) {
            user.updateLastSeen();
            user.goOnline();
            log.debug("Ping received from user: {}", user.getUsername());
        } else {
            log.warn("Ping received from unknown session: {}", sessionId);
        }
    }

    /**
     * Get room presence information using mapper
     * 
     * @param roomId Room identifier
     * @return RoomPresenceDTO with list of users
     */
    public RoomPresenceDTO getRoomPresence(String roomId) {
        log.info("Getting presence for room: {}", roomId);

        Set<String> userIds = roomToUsers.getOrDefault(roomId, Collections.emptySet());

        // Convert User entities to DTOs using mapper
        List<UserDTO> users = userIds.stream()
                .map(userIdToUser::get)
                .filter(Objects::nonNull)
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        // Use mapper to create RoomPresenceDTO
        return roomPresenceMapper.toRoomPresenceDTO(roomId, users);
    }

    /**
     * Get all online users using mapper
     * 
     * @return List of online UserDTOs
     */
    public List<UserDTO> getOnlineUsers() {
        log.info("Getting all online users");

        return userIdToUser.values().stream()
                .filter(User::isOnline)
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Handle user disconnect
     * 
     * @param sessionId WebSocket session ID
     */
    public void handleDisconnect(String sessionId) {
        User user = sessionToUser.get(sessionId);
        if (user == null) {
            log.warn("Disconnect event for unknown session: {}", sessionId);
            return;
        }

        log.info("Handling disconnect for user: {} (session: {})", user.getUsername(), sessionId);

        // Remove from room
        try {
            handleLeaveRoom(sessionId);
        } catch (UserNotFoundException e) {
            log.warn("User already removed during disconnect: {}", e.getMessage());
        }

        // Clean up user data
        sessionToUser.remove(sessionId);
        userIdToUser.remove(user.getUserId());

        log.info("User {} fully disconnected and removed", user.getUsername());
    }

    /**
     * Broadcast current room presence to all members of the room
     *
     * @param roomId Room identifier
     */
    private void broadcastRoomPresence(String roomId) {
        try {
            RoomPresenceDTO presence = getRoomPresence(roomId);
            WebSocketMessage message = WebSocketMessage.roomPresenceResponse(roomId, presence);
            broadcastToRoom(roomId, message);
            log.info("Broadcasted presence update for room: {} ({} users)", roomId, presence.getUserCount());
        } catch (Exception e) {
            log.error("Failed to broadcast presence update for room {}: {}", roomId, e.getMessage());
        }
    }

    /**
     * Broadcast message to all users in a room
     * 
     * @param roomId  Room identifier
     * @param message WebSocket message to broadcast
     */
    private void broadcastToRoom(String roomId, WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        log.debug("Broadcasted message to room {}: {}", roomId, message.getType());
    }

    /**
     * Get current system statistics
     * 
     * @return Map of system stats
     */
    public Map<String, Object> getSystemStats() {
        long onlineUsersCount = userIdToUser.values().stream()
                .filter(User::isOnline)
                .count();

        return Map.of(
                "totalUsers", userIdToUser.size(),
                "activeSessions", sessionToUser.size(),
                "activeRooms", roomToUsers.size(),
                "onlineUsers", onlineUsersCount,
                "timestamp", LocalDateTime.now());
    }

    /**
     * Get user by session ID
     * 
     * @param sessionId WebSocket session ID
     * @return UserDTO or null if not found
     */
    public UserDTO getUserBySession(String sessionId) {
        User user = sessionToUser.get(sessionId);
        return user != null ? userMapper.toDto(user) : null;
    }

    /**
     * Get user by user ID
     * 
     * @param userId User identifier
     * @return UserDTO or null if not found
     */
    public UserDTO getUserById(String userId) {
        User user = userIdToUser.get(userId);
        return user != null ? userMapper.toDto(user) : null;
    }

    /**
     * Get all rooms
     * 
     * @return List of room IDs
     */
    public List<String> getAllRooms() {
        return new ArrayList<>(roomToUsers.keySet());
    }

    /**
     * Get user count in room
     * 
     * @param roomId Room identifier
     * @return Number of users in room
     */
    public int getRoomUserCount(String roomId) {
        Set<String> users = roomToUsers.get(roomId);
        return users != null ? users.size() : 0;
    }
}
