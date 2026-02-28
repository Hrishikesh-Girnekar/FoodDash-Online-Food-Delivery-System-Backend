package com.app.fooddash.repository;

import com.app.fooddash.entity.MenuItem;
import com.app.fooddash.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantAndIsAvailableTrue(Restaurant restaurant);

    List<MenuItem> findByRestaurant(Restaurant restaurant);
    
    
}
