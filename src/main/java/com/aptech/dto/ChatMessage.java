package com.aptech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight chat message payload for room conversations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    private String roomId;

    private String userId;

    private String username;

    private String content;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
}
