package com.app.fooddash.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.OwnerDashboardStatsResponse;
import com.app.fooddash.dto.response.RecentOrderResponse;
import com.app.fooddash.dto.response.RestaurantResponse;

public interface RestaurantService {

    void createRestaurant(CreateRestaurantRequest request, MultipartFile image);

    void approveRestaurant(Long restaurantId);

    List<RestaurantResponse> getApprovedRestaurants();
    
    List<RestaurantResponse> getAllRestaurants();
    
    List<RestaurantResponse> getOwnerRestaurants();

    RestaurantResponse updateRestaurant(Long id, CreateRestaurantRequest request, MultipartFile image);

    void deleteRestaurant(Long id);

    void toggleRestaurantStatus(Long id);
    
    RestaurantResponse getRestaurantById(Long id);
    
    OwnerDashboardStatsResponse getOwnerDashboardStats();
    
    List<RecentOrderResponse> getRecentOrders(String email);
}
