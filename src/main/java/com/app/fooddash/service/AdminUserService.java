package com.app.fooddash.service;

import java.util.List;

import com.app.fooddash.dto.response.UserResponseAdmin;
import com.app.fooddash.enums.RoleType;

public interface AdminUserService {
	List<UserResponseAdmin> getUsers(String search, RoleType role);

	void banUser(Long id);

	void unbanUser(Long id);

}
