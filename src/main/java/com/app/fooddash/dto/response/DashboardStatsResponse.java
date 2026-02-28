package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardStatsResponse {

    private Long totalUsers;
    private Long activeRestaurants;
    private Long totalOrders;
    private Double totalRevenue;
    private Long pendingApprovals;

}