package com.app.fooddash.service;

import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.enums.RestaurantStatus;

import java.util.List;

public interface AdminRestaurantService {

    List<RestaurantResponse> getAllRestaurants();

    List<RestaurantResponse> getRestaurantsByStatus(RestaurantStatus status);

    void updateRestaurantStatus(Long id, RestaurantStatus status);

    void deleteRestaurant(Long id);
}