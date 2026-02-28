package com.app.fooddash.dto.response;

import com.app.fooddash.enums.RoleType;
import com.app.fooddash.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserResponseAdmin {

    private Long id;
    private String name;
    private String email;
    private Set<RoleType> roles;
    private UserStatus status;
    private LocalDateTime createdAt;
}