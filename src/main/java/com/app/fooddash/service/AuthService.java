package com.app.fooddash.service;

import com.app.fooddash.dto.request.ChangePasswordRequest;
import com.app.fooddash.dto.request.LoginRequest;
import com.app.fooddash.dto.request.RegisterRequest;
import com.app.fooddash.dto.request.UpdateProfileRequest;
import com.app.fooddash.dto.response.LoginResponse;
import com.app.fooddash.dto.response.ProfileResponse;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
    
    LoginResponse refreshToken(String refreshToken);
    
    ProfileResponse getProfile();
    
    ProfileResponse updateProfile(UpdateProfileRequest request);
    
    void changePassword(ChangePasswordRequest request);
}
