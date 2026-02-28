package com.app.fooddash.controller;

import com.app.fooddash.dto.request.ChangePasswordRequest;
import com.app.fooddash.dto.request.LoginRequest;
import com.app.fooddash.dto.request.RegisterRequest;
import com.app.fooddash.dto.request.UpdateProfileRequest;
import com.app.fooddash.dto.response.LoginResponse;
import com.app.fooddash.dto.response.ProfileResponse;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
		authService.register(request);

		return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", null));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		System.out.println(request.getEmail());
		System.out.println(response.getAccessToken());

		return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
	}

	@GetMapping("/test")
	public ResponseEntity<ApiResponse<Void>> test() {

		return ResponseEntity.ok(new ApiResponse<>(true, "Authenticated Successfully", null));
	}

	@GetMapping("/admin-test")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> adminTest() {

		return ResponseEntity.ok(new ApiResponse<>(true, "Admin Access Granted", null));
	}

	@GetMapping("/profile")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {

		ProfileResponse response = authService.getProfile();

		return ResponseEntity.ok(new ApiResponse<>(true, "Profile fetched successfully", response));
	}

	@PutMapping("/profile")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
			@Valid @RequestBody UpdateProfileRequest request) {

		ProfileResponse response = authService.updateProfile(request);

		return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", response));
	}

	@PutMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

		authService.changePassword(request);

		return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
	}

}
