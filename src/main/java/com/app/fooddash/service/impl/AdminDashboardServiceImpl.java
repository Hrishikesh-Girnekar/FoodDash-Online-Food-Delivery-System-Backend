package com.app.fooddash.service.impl;

import com.app.fooddash.dto.response.DashboardStatsResponse;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.repository.OrderRepository;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {

    	log.info("Fetching admin dashboard statistics");
        Long totalUsers = userRepository.count();
        Long activeRestaurants = restaurantRepository.countByStatus(RestaurantStatus.APPROVED);
        Long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();
        Long pendingApprovals = restaurantRepository.countByStatus(RestaurantStatus.PENDING);

        double safeRevenue = totalRevenue != null ? totalRevenue : 0.0;
        log.info("Dashboard stats calculated: totalUsers={}, activeRestaurants={}, totalOrders={}, totalRevenue={}, pendingApprovals={}",
                totalUsers, activeRestaurants, totalOrders, safeRevenue, pendingApprovals);
        
        return new DashboardStatsResponse(
                totalUsers,
                activeRestaurants,
                totalOrders,
                safeRevenue,
                pendingApprovals
        );
    }
}