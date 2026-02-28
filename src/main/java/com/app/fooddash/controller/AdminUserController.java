package com.app.fooddash.controller;

import com.app.fooddash.dto.response.UserResponseAdmin;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<UserResponseAdmin> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RoleType role
    ) {
        return adminUserService.getUsers(search, role);
    }

    @PutMapping("/{id}/ban")
    public void banUser(@PathVariable Long id) {
        adminUserService.banUser(id);
    }

    @PutMapping("/{id}/unban")
    public void unbanUser(@PathVariable Long id) {
        adminUserService.unbanUser(id);
    }
}