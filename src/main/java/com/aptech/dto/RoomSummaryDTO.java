package com.aptech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Minimal representation of a room for listing and navigation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roomId;

    @Builder.Default
    private int userCount = 0;
}
