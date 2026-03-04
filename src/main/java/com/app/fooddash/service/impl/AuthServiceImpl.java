package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.ChangePasswordRequest;
import com.app.fooddash.dto.request.LoginRequest;
import com.app.fooddash.dto.request.RegisterRequest;
import com.app.fooddash.dto.request.UpdateProfileRequest;
import com.app.fooddash.dto.response.LoginResponse;
import com.app.fooddash.dto.response.ProfileResponse;
import com.app.fooddash.dto.response.RestaurantSummaryDto;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.Role;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.RoleRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.security.CustomUserDetailsService;
import com.app.fooddash.security.JwtService;
import com.app.fooddash.service.AuthService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final RestaurantRepository restaurantRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@Override
	public void register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BadRequestException("Email already registered");
		}

		Role role = roleRepository.findByName(request.getRole())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		User user = new User();
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.getRoles().add(role);

		userRepository.save(user);
	}

//    @Override
//    public LoginResponse login(LoginRequest request) {
//
//        // 1️⃣ Authenticate user
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//
//        // 2️⃣ Get authenticated user (Spring Security User)
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//        // 3️⃣ Extract role safely
//        String role = userDetails.getAuthorities()
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Role not found"))
//                .getAuthority();
//
//        // 4️⃣ Generate token
//        String token = jwtService.generateToken(userDetails.getUsername());
//
//        return new LoginResponse(token, role);
//    }

//    @Override
//    public LoginResponse login(LoginRequest request) {
//
//        // 1️⃣ Authenticate user
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//
//        // 2️⃣ Get authenticated user
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//        // 3️⃣ Extract role
//        String role = userDetails.getAuthorities()
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("Role not found"))
//                .getAuthority();
//
//        // 4️⃣ Fetch full user entity from DB
//        User user = userRepository.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new BadRequestException("User not found"));
//
//        // 5️⃣ Generate token
//        String token = jwtService.generateToken(userDetails.getUsername());
//
//        // 6️⃣ Return token + role + name
//        return new LoginResponse(
//                token,
//                role,
//                user.getFullName(),
//                
//        );
//    }
	@Override
	public LoginResponse login(LoginRequest request) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		String role = userDetails.getAuthorities().stream().findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Role not found")).getAuthority();

		User user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new BadRequestException("User not found"));

//		String token = jwtService.generateToken(userDetails.getUsername());
		String accessToken = jwtService.generateAccessToken(user.getEmail());
		String refreshToken = jwtService.generateRefreshToken(user.getEmail());

		List<RestaurantSummaryDto> restaurantDtos = List.of();

		if (role.equals("RESTAURANT_OWNER")) {

			List<Restaurant> restaurants = restaurantRepository.findByOwner(user);

			restaurantDtos = restaurants.stream()
					.map(r -> RestaurantSummaryDto.builder().id(r.getId()).name(r.getName()).build()).toList();
		}

//        return LoginResponse.builder()
//                .accessToken(token)
//                .role(role)
//                .fullname(user.getFullName())
//                .restaurants(restaurantDtos)
//                .build();
		return new LoginResponse(accessToken, refreshToken, role, user.getFullName(), restaurantDtos);
	}

//	@Override
//	public LoginResponse refreshToken(String refreshToken) {
//
//		try {
//			String email = jwtService.extractUsername(refreshToken);
//
//			if (!jwtService.isTokenValid(refreshToken, email)) {
//				throw new RuntimeException("Invalid refresh token");
//			}
//
//			User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
//
//			String newAccessToken = jwtService.generateAccessToken(email);
//			System.out.println("Refresh token received: " + refreshToken);
//			System.out.println("Extracted email: " + email);
//			
//			 // ✅ FIX: Get role safely from Set
////		    String roleName = user.getRoles()
////		            .stream()
////		            .findFirst()
////		            .map(Enum::name)
////		            .orElse("USER");
//
//			List<RestaurantSummaryDto> restaurants = List.of();
//			
//
//
//			if (((Enum<RoleType>) user.getRoles()).name().equals("OWNER")) {
//				restaurants = user.getRestaurants().stream().map(r -> new RestaurantSummaryDto(r.getId(), r.getName()))
//						.toList();
//			}
//
//			return new LoginResponse(newAccessToken, refreshToken, ((Enum<RoleType>) user.getRoles()).name(),
//					user.getFullName(), restaurants);
//
//		} catch (Exception e) {
//			e.printStackTrace(); 
//			throw new RuntimeException("Refresh failed");
//		}
//	}
	@Override
	public LoginResponse refreshToken(String refreshToken) {

		try {
			String email = jwtService.extractUsername(refreshToken);

			if (!jwtService.isTokenValid(refreshToken, email)) {
				throw new RuntimeException("Invalid refresh token");
			}

			User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

			String newAccessToken = jwtService.generateAccessToken(email);

			System.out.println("Refresh token received: " + refreshToken);
			System.out.println("Extracted email: " + email);

			// ✅ FIX: Extract role name from Role entity
			String roleName = user.getRoles().stream().findFirst().map(role -> role.getName().name()) // <-- IMPORTANT
					.orElse("USER");

			List<RestaurantSummaryDto> restaurants = List.of();

			if (roleName.equals("OWNER")) {
				restaurants = user.getRestaurants().stream().map(r -> new RestaurantSummaryDto(r.getId(), r.getName()))
						.toList();
			}

			return new LoginResponse(newAccessToken, refreshToken, roleName, user.getFullName(), restaurants);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Refresh failed");
		}
	}

	private User getCurrentUser() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		return userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));
	}

	@Override
	public ProfileResponse getProfile() {

		User user = getCurrentUser();

		RoleType roleType = user.getRoles().stream().findFirst().map(role -> role.getName()) // adjust getter if needed
				.orElse(null);

		return ProfileResponse.builder().id(user.getId()).name(user.getFullName()).email(user.getEmail()).role(roleType)
				.build();
	}

	@Override
	public ProfileResponse updateProfile(UpdateProfileRequest request) {

		User user = getCurrentUser();

		user.setFullName(request.getFullName());
		userRepository.save(user);

		RoleType roleType = user.getRoles().stream().findFirst().map(role -> role.getName()) // adjust getter if needed
				.orElse(null);

		return ProfileResponse.builder().id(user.getId()).name(user.getFullName()).email(user.getEmail()).role(roleType)
				.build();
	}

	@Override
	public void changePassword(ChangePasswordRequest request) {

		User user = getCurrentUser();

		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
			throw new BadRequestException("Current password is incorrect");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));

		userRepository.save(user);
	}

}
