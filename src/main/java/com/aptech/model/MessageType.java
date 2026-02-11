package com.aptech.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing different types of WebSocket messages
 * Using Lombok for clean enum implementation
 */
@Getter
@RequiredArgsConstructor
public enum MessageType {
    JOIN("User joining a room"),
    LEAVE("User leaving a room"),
    PING("Heartbeat/ping message"),
    SYSTEM("System-generated messages"),
    ROOM_PRESENCE("Request/response for room presence"),
    ONLINE_USERS("Request/response for online users"),
    ROOM_LIST("Request/response for room catalog"),
    CHAT("Room chat message"),
    CHAT_HISTORY("Room chat history"),
    CALL_SIGNAL("WebRTC signalling message");

    private final String description;
}
