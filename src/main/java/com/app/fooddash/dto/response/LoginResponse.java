package com.app.fooddash.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String role;
    private String fullname;
    private List<RestaurantSummaryDto> restaurants;
   
    
}
