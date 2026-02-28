package com.app.fooddash.service.impl;

import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.service.AdminRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminRestaurantServiceImpl implements AdminRestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RestaurantResponse> getRestaurantsByStatus(RestaurantStatus status) {
        return restaurantRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void updateRestaurantStatus(Long id, RestaurantStatus status) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

     // Optional safety check
        if (restaurant.getStatus() != RestaurantStatus.PENDING) {
            throw new RuntimeException("Only pending restaurants can be approved");
        }
        restaurant.setStatus(status);
        restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurantRepository.delete(restaurant);
    }

    // ===== DTO Mapper =====
    private RestaurantResponse mapToResponse(Restaurant restaurant) {

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .phone(restaurant.getPhone())
                .cuisine(restaurant.getCuisine())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .costForTwo(restaurant.getCostForTwo())
                .rating(restaurant.getRating())
                .totalReviews(restaurant.getTotalReviews())
                .status(restaurant.getStatus())
                .imageUrl(restaurant.getImageUrl())
                .build();
    }
}