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
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

	private final UserRepository userRepository;

	public List<UserResponseAdmin> getUsers(String search, RoleType role) {

		log.info("Admin fetching users. search={}, role={}", search, role);

		List<User> users = userRepository.findAll();

		if (search != null && !search.isBlank()) {
			log.info("Applying search filter: {}", search);

			users = users.stream()
					.filter(u -> u.getFullName().toLowerCase().contains(search.toLowerCase())
							|| u.getEmail().toLowerCase().contains(search.toLowerCase()))
					.collect(Collectors.toList());
		}

		if (role != null) {
			log.info("Applying role filter: {}", role);

			users = users.stream()
					.filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == role))
					.collect(Collectors.toList());
		}

		log.info("Total users fetched after filtering: {}", users.size());

		return users.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public void banUser(Long id) {

		log.info("Admin banning user. userId={}", id);

		User user = getUserById(id);

		user.setStatus(UserStatus.BANNED);
		userRepository.save(user);

		log.warn("User banned successfully. userId={}, email={}", user.getId(), user.getEmail());
	}

	public void unbanUser(Long id) {

		log.info("Admin unbanning user. userId={}", id);

		User user = getUserById(id);

		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);

		log.info("User unbanned successfully. userId={}, email={}", user.getId(), user.getEmail());
	}

	private User getUserById(Long id) {

		return userRepository.findById(id)
				.orElseThrow(() -> {
					log.error("User not found with id={}", id);
					return new RuntimeException("User not found");
				});
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