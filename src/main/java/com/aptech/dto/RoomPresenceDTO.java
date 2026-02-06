package com.aptech.dto;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for room presence information
 * Represents all users in a specific room
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RoomPresenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roomId;

    @Builder.Default
    private int userCount = 0;

    @Builder.Default
    private List<UserDTO> users = new ArrayList<>();

    /**
     * Add user to room presence
     */
    public void addUser(UserDTO user) {
        this.users.add(user);
        this.userCount = this.users.size();
    }

    /**
     * Check if room is empty
     */
    public boolean isEmpty() {
        return userCount == 0 || users.isEmpty();
    }
}
