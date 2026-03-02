package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerDashboardStatsResponse {

    private Long todayOrders;
    private Long totalOrders;
    private Double totalRevenue;
    private Double averageRating;
}