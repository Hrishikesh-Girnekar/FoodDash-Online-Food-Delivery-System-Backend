package com.app.fooddash.service;

import java.util.List;
import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.OwnerDashboardStatsResponse;
import com.app.fooddash.dto.response.RecentOrderResponse;
import com.app.fooddash.dto.response.RestaurantResponse;

public interface RestaurantService {

    void createRestaurant(CreateRestaurantRequest request);

    void approveRestaurant(Long restaurantId);

    List<RestaurantResponse> getApprovedRestaurants();
    
    List<RestaurantResponse> getAllRestaurants();
    
    List<RestaurantResponse> getOwnerRestaurants();

    void updateRestaurant(Long id, CreateRestaurantRequest request);

    void deleteRestaurant(Long id);

    void toggleRestaurantStatus(Long id);
    
    RestaurantResponse getRestaurantById(Long id);
    
    OwnerDashboardStatsResponse getOwnerDashboardStats();
    
    List<RecentOrderResponse> getRecentOrders(String email);
}
