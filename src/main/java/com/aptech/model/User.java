package com.aptech.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity representing a user in the system
 * Using comprehensive Lombok annotations for clean code
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = { "userId", "sessionId" })
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private String userId;

    @NonNull
    private String username;

    @NonNull
    private String sessionId;

    private String currentRoom;

    @Builder.Default
    private LocalDateTime lastSeen = LocalDateTime.now();

    @Builder.Default
    private boolean online = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Update last seen timestamp
     */
    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }

    /**
     * Mark user as offline
     */
    public void goOffline() {
        this.online = false;
        this.lastSeen = LocalDateTime.now();
    }

    /**
     * Mark user as online
     */
    public void goOnline() {
        this.online = true;
        this.lastSeen = LocalDateTime.now();
    }
}
