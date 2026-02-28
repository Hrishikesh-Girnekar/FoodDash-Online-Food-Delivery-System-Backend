package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.AddToCartRequest;
import com.app.fooddash.dto.response.CartItemResponse;
import com.app.fooddash.dto.response.CartResponse;
import com.app.fooddash.entity.*;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.repository.*;
import com.app.fooddash.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MenuItemRepository menuItemRepository;
	private final UserRepository userRepository;

	@Override
	public void addToCart(AddToCartRequest request) {

		// 1️ Get logged-in user
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// 2️ Fetch menu item
		MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

		Restaurant restaurant = menuItem.getRestaurant();

		// 3️ Get or create cart
		Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUser(user);
			return cartRepository.save(newCart);
		});

		// ✅ If cart has no restaurant, assign it
		if (cart.getRestaurant() == null) {
			cart.setRestaurant(restaurant);
		}

		// ✅ If cart has different restaurant, reject
		else if (!cart.getRestaurant().getId().equals(restaurant.getId())) {
			throw new BadRequestException("You cannot add items from different restaurants");
		}

		// 5️ Check if item already exists
		CartItem cartItem = cartItemRepository.findByCartAndMenuItem(cart, menuItem).orElseGet(() -> {
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setMenuItem(menuItem);
			newItem.setQuantity(0);
			return newItem;
		});

		// 6️ Update quantity
		cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());

		cartItemRepository.save(cartItem);
	}

	@Override
	public CartResponse viewCart() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new BadRequestException("Cart is empty"));

		var items = cart.getItems();

		var itemResponses = items.stream().map(item -> {

			var price = item.getMenuItem().getPrice();
			var quantity = item.getQuantity();
			var total = price.multiply(java.math.BigDecimal.valueOf(quantity));

			return new CartItemResponse(item.getMenuItem().getId(), item.getMenuItem().getName(), price, quantity,
					total);
		}).toList();

		var cartTotal = itemResponses.stream().map(CartItemResponse::getTotal).reduce(java.math.BigDecimal.ZERO,
				java.math.BigDecimal::add);

		return new CartResponse(cart.getRestaurant().getName(), itemResponses, cartTotal);
	}

}
