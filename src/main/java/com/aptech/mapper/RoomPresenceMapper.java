package com.aptech.mapper;

import com.aptech.dto.RoomPresenceDTO;
import com.aptech.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for RoomPresence information
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomPresenceMapper {

    @Mapping(target = "roomId", source = "roomId")
    @Mapping(target = "users", source = "users")
    @Mapping(target = "userCount", expression = "java(users.size())")
    RoomPresenceDTO toRoomPresenceDTO(String roomId, List<UserDTO> users);
}
