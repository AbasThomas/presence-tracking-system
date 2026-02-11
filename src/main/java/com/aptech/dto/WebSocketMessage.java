package com.aptech.dto;

import com.aptech.model.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for WebSocket messages
 * Using Lombok and validation annotations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Message type is required")
    private MessageType type;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Username is required")
    private String username;

    private String roomId;

    private String targetUserId;

    /**
     * Optional discriminator for signalling (OFFER / ANSWER / ICE / END).
     */
    private String signalType;

    private String content;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    private Object data;

    /**
     * Factory method for system messages
     */
    public static WebSocketMessage systemMessage(String content, String roomId) {
        return WebSocketMessage.builder()
                .type(MessageType.SYSTEM)
                .content(content)
                .roomId(roomId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for room presence response
     */
    public static WebSocketMessage roomPresenceResponse(String roomId, Object data) {
        return WebSocketMessage.builder()
                .type(MessageType.ROOM_PRESENCE)
                .roomId(roomId)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for online users response
     */
    public static WebSocketMessage onlineUsersResponse(Object data) {
        return WebSocketMessage.builder()
                .type(MessageType.ONLINE_USERS)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for chat broadcasts.
     */
    public static WebSocketMessage chatBroadcast(Object data, String roomId) {
        return WebSocketMessage.builder()
                .type(MessageType.CHAT)
                .roomId(roomId)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory for chat history responses.
     */
    public static WebSocketMessage chatHistoryResponse(String roomId, Object data) {
        return WebSocketMessage.builder()
                .type(MessageType.CHAT_HISTORY)
                .roomId(roomId)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory for room list responses.
     */
    public static WebSocketMessage roomListResponse(Object data) {
        return WebSocketMessage.builder()
                .type(MessageType.ROOM_LIST)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory for call signalling payloads.
     */
    public static WebSocketMessage callSignal(String roomId,
                                              String userId,
                                              String username,
                                              String targetUserId,
                                              String signalType,
                                              Object data) {
        return WebSocketMessage.builder()
                .type(MessageType.CALL_SIGNAL)
                .roomId(roomId)
                .userId(userId)
                .username(username)
                .targetUserId(targetUserId)
                .signalType(signalType)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
