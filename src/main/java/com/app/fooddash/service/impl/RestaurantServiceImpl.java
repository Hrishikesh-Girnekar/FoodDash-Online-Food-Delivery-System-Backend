package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.RestaurantService;
import com.app.fooddash.util.AuthUtil;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    @Override
    public void createRestaurant(CreateRestaurantRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Restaurant restaurant = new Restaurant();

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setPhone(request.getPhone());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setCostForTwo(request.getCostForTwo());
        restaurant.setImageUrl(request.getImageUrl());

        restaurant.setRating(4.2);
        restaurant.setTotalReviews(120);
        restaurant.setIsOpen(true);
        restaurant.setIsApproved(false);

        restaurant.setOwner(owner);

        restaurantRepository.save(restaurant);
    }

    @Override
    public void approveRestaurant(Long restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setIsApproved(true);

        restaurantRepository.save(restaurant);
    }

    @Override
    public List<RestaurantResponse> getApprovedRestaurants() {

        return restaurantRepository
                .findByStatus(RestaurantStatus.APPROVED)
                .stream()
                .map(r -> new RestaurantResponse(
                        r.getId(),
                        r.getName(),
                        r.getDescription(),
                        r.getPhone(),
                        r.getCuisine(),
                        r.getAddress(),
                        r.getCity(),
                        r.getOpeningTime(),
                        r.getClosingTime(),
                        r.getCostForTwo(),
                        r.getImageUrl(),
                        r.getRating(),
                        r.getTotalReviews(),
                        r.getIsOpen(),
                        r.getStatus(),
                        r.getCreatedAt()
                ))
                .toList();
    }
    
    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Override
    public List<RestaurantResponse> getOwnerRestaurants() {

        User owner = authUtil.getCurrentUser();

        return restaurantRepository.findByOwner(owner)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Override
    public void deleteRestaurant(Long id) {

        User owner = authUtil.getCurrentUser();

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not allowed to delete this restaurant");
        }

        restaurantRepository.delete(restaurant);
    }
    
    @Override
    @Transactional
    public void toggleRestaurantStatus(Long id) {

        User owner = authUtil.getCurrentUser();

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not allowed to update this restaurant");
        }

        // ✅ Null-safe toggle
        Boolean currentStatus = restaurant.getIsOpen();

        if (currentStatus == null) {
            restaurant.setIsOpen(true);  // default fallback
        } else {
            restaurant.setIsOpen(!currentStatus);
        }
    }
    
    @Override
    public void updateRestaurant(Long id, CreateRestaurantRequest request) {

        User owner = authUtil.getCurrentUser();

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not allowed to update this restaurant");
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setPhone(request.getPhone());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setCostForTwo(request.getCostForTwo());
        restaurant.setImageUrl(request.getImageUrl());

        restaurantRepository.save(restaurant);
    }
    
    @Override
    public RestaurantResponse getRestaurantById(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        return mapToResponse(restaurant);
    }

    // ================= MAPPER METHOD =================
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
                .status(restaurant.getStatus())
                .imageUrl(restaurant.getImageUrl())
                .isOpen(restaurant.getIsOpen())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}