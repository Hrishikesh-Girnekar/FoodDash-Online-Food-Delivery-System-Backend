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
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MenuItemRepository menuItemRepository;
	private final UserRepository userRepository;

	@Override
	public void addToCart(AddToCartRequest request) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Adding item to cart. user={}, menuItemId={}, quantity={}",
				email, request.getMenuItemId(), request.getQuantity());

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.error("User not found with email={}", email);
					return new ResourceNotFoundException("User not found");
				});

		MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
				.orElseThrow(() -> {
					log.error("Menu item not found with id={}", request.getMenuItemId());
					return new ResourceNotFoundException("Menu item not found");
				});

		Restaurant restaurant = menuItem.getRestaurant();

		Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
			log.info("Cart not found. Creating new cart for user={}", email);
			Cart newCart = new Cart();
			newCart.setUser(user);
			return cartRepository.save(newCart);
		});

		if (cart.getRestaurant() == null) {
			log.info("Assigning restaurant to cart. user={}, restaurantId={}", email, restaurant.getId());
			cart.setRestaurant(restaurant);
		}

		else if (!cart.getRestaurant().getId().equals(restaurant.getId())) {
			log.warn("User attempted to add items from different restaurant. user={}, existingRestaurantId={}, newRestaurantId={}",
					email, cart.getRestaurant().getId(), restaurant.getId());
			throw new BadRequestException("You cannot add items from different restaurants");
		}

		CartItem cartItem = cartItemRepository.findByCartAndMenuItem(cart, menuItem).orElseGet(() -> {
			log.info("Item not in cart. Creating new cart item. user={}, menuItemId={}", email, menuItem.getId());
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setMenuItem(menuItem);
			newItem.setQuantity(0);
			return newItem;
		});

		cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());

		cartItemRepository.save(cartItem);

		log.info("Item added to cart successfully. user={}, menuItemId={}, newQuantity={}",
				email, menuItem.getId(), cartItem.getQuantity());
	}

	@Override
	public CartResponse viewCart() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Fetching cart for user={}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.error("User not found with email={}", email);
					return new ResourceNotFoundException("User not found");
				});

		Cart cart = cartRepository.findByUser(user)
				.orElseThrow(() -> {
					log.warn("Cart is empty for user={}", email);
					return new BadRequestException("Cart is empty");
				});

		var items = cart.getItems();

		var itemResponses = items.stream().map(item -> {

			var price = item.getMenuItem().getPrice();
			var quantity = item.getQuantity();
			var total = price.multiply(java.math.BigDecimal.valueOf(quantity));

			return new CartItemResponse(
					item.getMenuItem().getId(),
					item.getMenuItem().getName(),
					price,
					quantity,
					total
			);
		}).toList();

		var cartTotal = itemResponses.stream()
				.map(CartItemResponse::getTotal)
				.reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

		log.info("Cart fetched successfully. user={}, itemsCount={}, totalAmount={}",
				email, itemResponses.size(), cartTotal);

		return new CartResponse(cart.getRestaurant().getName(), itemResponses, cartTotal);
	}
}