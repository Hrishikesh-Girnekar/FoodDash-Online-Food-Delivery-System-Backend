package com.app.fooddash.controller;

import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.DashboardStatsResponse;
import com.app.fooddash.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {

        DashboardStatsResponse response = adminDashboardService.getDashboardStats();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Dashboard stats fetched successfully", response)
        );
    }
}