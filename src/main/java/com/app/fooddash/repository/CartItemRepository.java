package com.app.fooddash.repository;

import com.app.fooddash.entity.CartItem;
import com.app.fooddash.entity.Cart;
import com.app.fooddash.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);
}
