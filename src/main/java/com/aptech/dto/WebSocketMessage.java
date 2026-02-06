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
}
