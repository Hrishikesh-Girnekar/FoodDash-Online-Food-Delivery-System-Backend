package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.MenuItemResponse;
import com.app.fooddash.entity.MenuItem;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.exception.UnauthorizedException;
import com.app.fooddash.repository.MenuItemRepository;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.MenuService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

	private final MenuItemRepository menuItemRepository;
	private final RestaurantRepository restaurantRepository;
	private final UserRepository userRepository;

	@Override
	public void addMenuItem(CreateMenuItemRequest request) {

	    String email = SecurityContextHolder.getContext().getAuthentication().getName();

	    User owner = userRepository.findByEmail(email)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

	    Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
	            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

	    if (!restaurant.getOwner().getId().equals(owner.getId())) {
	        throw new UnauthorizedException("You can only add menu to your own restaurant");
	    }

	    MenuItem item = new MenuItem();
	    item.setName(request.getName());
	    item.setDescription(request.getDescription());
	    item.setPrice(request.getPrice());
	    item.setCategory(request.getCategory());
	    item.setIsVeg(request.getIsVeg());
	    item.setIsBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false);
	    item.setImageUrl(request.getImageUrl());
	    item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
	    item.setRestaurant(restaurant);

	    menuItemRepository.save(item);
	}

	@Override
	public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {

	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
	    System.out.println(restaurant.getIsApproved());

	    if (!restaurant.getIsApproved()) {
	        throw new BadRequestException("Restaurant not approved yet");
	    }

	    return menuItemRepository.findByRestaurant(restaurant)
	            .stream()
	            .map(item -> new MenuItemResponse(
	                    item.getId(),
	                    item.getName(),
	                    item.getDescription(),
	                    item.getPrice(),
	                    item.getCategory(),
	                    item.getIsVeg(),
	                    item.getIsBestseller(),
	                    item.getImageUrl(),
	                    item.getIsAvailable()
	            ))
	            .toList();
	}
	
	@Override
	public void updateMenuItem(Long id, CreateMenuItemRequest request) {

	    MenuItem item = menuItemRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Menu item not found"));

	    item.setName(request.getName());
	    item.setDescription(request.getDescription());
	    item.setPrice(request.getPrice());
	    item.setCategory(request.getCategory());
	    item.setIsVeg(request.getIsVeg());
	    item.setIsAvailable(request.getIsAvailable());

	    menuItemRepository.save(item);
	}

	@Override
	public void deleteMenuItem(Long id) {
	    menuItemRepository.deleteById(id);
	}

	@Override
	public void toggleAvailability(Long id) {

	    MenuItem item = menuItemRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Menu item not found"));

	    item.setIsAvailable(!item.getIsAvailable());

	    menuItemRepository.save(item);
	}

}
