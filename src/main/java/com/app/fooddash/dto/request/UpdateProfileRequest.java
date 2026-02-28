package com.app.fooddash.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    @NotBlank
    private String fullName;

}


