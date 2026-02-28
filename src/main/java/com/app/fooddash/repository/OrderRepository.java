package com.app.fooddash.repository;

import com.app.fooddash.entity.Order;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
    
    List<Order> findByRestaurant(Restaurant restaurant);
    
    List<Order> findAllByOrderByCreatedAtDesc();
    
    List<Order> findByDeliveryPartner(User deliveryPartner);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();



}
