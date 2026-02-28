package com.app.fooddash.repository;

import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RestaurantStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwner(User owner);

//    List<Restaurant> findByIsApprovedTrue();
    
    List<Restaurant> findByStatus(RestaurantStatus status);
    
    Optional<Restaurant> findById(Long id);
    
    Long countByStatus(RestaurantStatus status);
}
