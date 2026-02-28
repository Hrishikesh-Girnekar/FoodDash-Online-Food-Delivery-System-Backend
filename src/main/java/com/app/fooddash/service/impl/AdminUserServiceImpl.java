package com.app.fooddash.service.impl;

import com.app.fooddash.service.AdminUserService;
import com.app.fooddash.dto.response.UserResponseAdmin;
import com.app.fooddash.entity.Role;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.enums.UserStatus;
import com.app.fooddash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

	private final UserRepository userRepository;

	public List<UserResponseAdmin> getUsers(String search, RoleType role) {

		List<User> users = userRepository.findAll();

		if (search != null && !search.isBlank()) {
			users = users.stream().filter(u -> u.getFullName().toLowerCase().contains(search.toLowerCase())
					|| u.getEmail().toLowerCase().contains(search.toLowerCase())).collect(Collectors.toList());
		}

		if (role != null) {
			users = users.stream().filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == role))
					.collect(Collectors.toList());
		}

		return users.stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	public void banUser(Long id) {
		User user = getUserById(id);
		user.setStatus(UserStatus.BANNED);
		userRepository.save(user);
	}

	public void unbanUser(Long id) {
		User user = getUserById(id);
		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);
	}

	private User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
	}

	private UserResponseAdmin mapToResponse(User user) {

	    Set<RoleType> roleTypes = user.getRoles()
	            .stream()
	            .map(Role::getName)
	            .collect(Collectors.toSet());

	    return UserResponseAdmin.builder()
	            .id(user.getId())
	            .name(user.getFullName())
	            .email(user.getEmail())
	            .roles(roleTypes)
	            .status(user.getStatus())
	            .createdAt(user.getCreatedAt())
	            .build();
	}

}
