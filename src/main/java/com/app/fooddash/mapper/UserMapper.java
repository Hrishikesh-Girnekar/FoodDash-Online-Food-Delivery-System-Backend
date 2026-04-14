package com.app.fooddash.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.app.fooddash.dto.response.ProfileResponse;
import com.app.fooddash.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "name", source = "fullName")
    @Mapping(target = "role", expression = "java(getRole(user))")
    ProfileResponse toProfileResponse(User user);

    // ✅ custom logic for role
    default com.app.fooddash.enums.RoleType getRole(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(role -> role.getName())
                .orElse(null);
    }
}