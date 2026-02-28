package com.app.fooddash.dto.response;

import java.util.Set;

import com.app.fooddash.enums.RoleType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {

    private Long id;
    private String name;
    private String email;
    private RoleType role;
}