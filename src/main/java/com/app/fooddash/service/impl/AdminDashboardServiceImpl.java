package com.app.fooddash.service.impl;

import com.app.fooddash.dto.response.DashboardStatsResponse;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.repository.OrderRepository;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {

        Long totalUsers = userRepository.count();
        Long activeRestaurants = restaurantRepository.countByStatus(RestaurantStatus.APPROVED);
        Long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();
        Long pendingApprovals = restaurantRepository.countByStatus(RestaurantStatus.PENDING);

        return new DashboardStatsResponse(
                totalUsers,
                activeRestaurants,
                totalOrders,
                totalRevenue != null ? totalRevenue : 0.0,
                pendingApprovals
        );
    }
}