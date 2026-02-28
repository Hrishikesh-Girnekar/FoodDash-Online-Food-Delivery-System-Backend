package com.app.fooddash.repository;

import com.app.fooddash.entity.Cart;
import com.app.fooddash.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}
