package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.*;
import com.app.fooddash.dto.response.LoginResponse;
import com.app.fooddash.dto.response.ProfileResponse;
import com.app.fooddash.dto.response.RestaurantSummaryDto;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.Role;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.mapper.RestaurantMapper;
import com.app.fooddash.mapper.UserMapper;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.RoleRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.security.JwtService;
import com.app.fooddash.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // ✅ Inject mappers
    private final UserMapper userMapper;
    private final RestaurantMapper restaurantMapper;

    @Override
    public void register(RegisterRequest request) {

        log.info("Register attempt for email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new BadRequestException("Email already registered");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> {
                    log.error("Role not found: {}", request.getRole());
                    return new ResourceNotFoundException("Role not found");
                });

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(role);

        userRepository.save(user);

        log.info("User registered successfully. email={}, role={}", request.getEmail(), request.getRole());
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for email={}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities().stream().findFirst()
                .orElseThrow(() -> {
                    log.error("Role not found during login for email={}", request.getEmail());
                    return new ResourceNotFoundException("Role not found");
                })
                .getAuthority();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found after authentication. email={}", userDetails.getUsername());
                    return new BadRequestException("User not found");
                });

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        List<RestaurantSummaryDto> restaurantDtos = List.of();

        if (role.equals("RESTAURANT_OWNER")) {
            List<Restaurant> restaurants = restaurantRepository.findByOwner(user);
            restaurantDtos = restaurantMapper.toSummaryDtoList(restaurants);
        }

        log.info("Login successful. email={}, role={}", user.getEmail(), role);

        return new LoginResponse(
                accessToken,
                refreshToken,
                role,
                user.getFullName(),
                restaurantDtos
        );
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {

        log.info("Refresh token attempt");

        try {
            String email = jwtService.extractUsername(refreshToken);

            if (!jwtService.isTokenValid(refreshToken, email)) {
                log.warn("Invalid refresh token attempt for email={}", email);
                throw new RuntimeException("Invalid refresh token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found during refresh. email={}", email);
                        return new RuntimeException("User not found");
                    });

            String newAccessToken = jwtService.generateAccessToken(email);

            String roleName = user.getRoles().stream()
                    .findFirst()
                    .map(role -> role.getName().name())
                    .orElse("USER");

            List<RestaurantSummaryDto> restaurants = List.of();

            if (roleName.equals("OWNER")) {
                restaurants = restaurantMapper.toSummaryDtoList(user.getRestaurants());
            }

            log.info("Token refreshed successfully for email={}", email);

            return new LoginResponse(
                    newAccessToken,
                    refreshToken,
                    roleName,
                    user.getFullName(),
                    restaurants
            );

        } catch (Exception e) {
            log.error("Refresh token failed: {}", e.getMessage(), e);
            throw new RuntimeException("Refresh failed");
        }
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Current user not found with email={}", email);
                    return new BadRequestException("User not found");
                });
    }

    @Override
    public ProfileResponse getProfile() {

        User user = getCurrentUser();

        log.info("Fetching profile for user={}", user.getEmail());

        return userMapper.toProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(UpdateProfileRequest request) {

        User user = getCurrentUser();

        log.info("Updating profile for user={}", user.getEmail());

        user.setFullName(request.getFullName());
        userRepository.save(user);

        log.info("Profile updated successfully for user={}", user.getEmail());

        return userMapper.toProfileResponse(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        User user = getCurrentUser();

        log.info("Password change attempt for user={}", user.getEmail());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Incorrect current password for user={}", user.getEmail());
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user={}", user.getEmail());
    }
}