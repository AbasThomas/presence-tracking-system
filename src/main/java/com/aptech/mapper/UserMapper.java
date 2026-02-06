package com.aptech.mapper;

import com.aptech.dto.UserDTO;
import com.aptech.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for User entity and DTO
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDTO toDto(User user);

    User toEntity(UserDTO dto);
}
