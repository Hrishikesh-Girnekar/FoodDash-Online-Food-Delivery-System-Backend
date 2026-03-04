package com.app.fooddash.repository;

import com.app.fooddash.entity.Order;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Objects;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
    
    List<Order> findByRestaurant(Restaurant restaurant);
    
    List<Order> findAllByOrderByCreatedAtDesc();
    
    List<Order> findByDeliveryPartner(User deliveryPartner);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();
    
    Long countByRestaurantIdIn(List<Long> restaurantIds);

    Long countByRestaurantIdInAndCreatedAtBetween(
            List<Long> restaurantIds,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.restaurant.id IN :ids")
    Double sumRevenueByRestaurantIds(@Param("ids") List<Long> ids);
    
//    List<Order> findByRestaurantOrderByCreatedAtAsc(Restaurant restaurant);
    List<Order> findTop3ByRestaurantInOrderByCreatedAtDesc(List<Restaurant> restaurants);



}
