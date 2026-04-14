package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.MenuItemResponse;
import com.app.fooddash.entity.MenuItem;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.exception.UnauthorizedException;
import com.app.fooddash.mapper.MenuMapper;
import com.app.fooddash.repository.MenuItemRepository;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.CloudinaryService;
import com.app.fooddash.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

	private final MenuItemRepository menuItemRepository;
	private final RestaurantRepository restaurantRepository;
	private final UserRepository userRepository;
	private final CloudinaryService cloudinaryService;
	private final MenuMapper menuMapper;

	@Override
	public void addMenuItem(CreateMenuItemRequest request, MultipartFile image) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Adding menu item. user={}, restaurantId={}", email, request.getRestaurantId());

		User owner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("User not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElseThrow(() -> {
			log.error("Restaurant not found with id={}", request.getRestaurantId());
			return new ResourceNotFoundException("Restaurant not found");
		});

		if (!restaurant.getOwner().getId().equals(owner.getId())) {
			log.warn("Unauthorized menu add attempt. user={}, restaurantId={}", email, request.getRestaurantId());
			throw new UnauthorizedException("You can only add menu to your own restaurant");
		}

		String imageUrl = null;

		if (image != null && !image.isEmpty()) {
			String publicId = "fooddash/menu/" + UUID.randomUUID();
			log.info("Uploading menu item image. publicId={}", publicId);

			imageUrl = cloudinaryService.uploadFile(image, "fooddash/menu", publicId);
		}

		MenuItem item = menuMapper.toEntity(request);

		item.setIsBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false);
		item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

		item.setImageUrl(imageUrl);
		item.setRestaurant(restaurant);

		menuItemRepository.save(item);

		log.info("Menu item added successfully. user={}, restaurantId={}", email, request.getRestaurantId());
	}

	@Override
	public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {

		log.info("Fetching menu for restaurantId={}", restaurantId);

		Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> {
			log.error("Restaurant not found with id={}", restaurantId);
			return new ResourceNotFoundException("Restaurant not found");
		});

		log.info("Restaurant approval status. restaurantId={}, status={}", restaurantId, restaurant.getStatus());

		if (restaurant.getStatus() != RestaurantStatus.APPROVED) {
			log.warn("Attempt to access menu of unapproved restaurant. restaurantId={}", restaurantId);
			throw new BadRequestException("Restaurant is not approved yet");
		}

		List<MenuItemResponse> result = menuMapper.toResponseList(menuItemRepository.findByRestaurant(restaurant));

		log.info("Fetched {} menu items for restaurantId={}", result.size(), restaurantId);

		return result;
	}

	@Override
	public void updateMenuItem(Long id, CreateMenuItemRequest request, MultipartFile image) {

		log.info("Updating menu item. itemId={}", id);

		MenuItem item = menuItemRepository.findById(id).orElseThrow(() -> {
			log.error("Menu item not found with id={}", id);
			return new RuntimeException("Menu item not found");
		});

		menuMapper.updateMenuItemFromDto(request, item);

		if (image != null && !image.isEmpty()) {

			try {
				String publicId = "fooddash/menu/" + item.getId();
				log.info("Updating menu item image. itemId={}, publicId={}", id, publicId);

				String newImageUrl = cloudinaryService.uploadFile(image, "fooddash/menu", publicId);
				item.setImageUrl(newImageUrl);

			} catch (Exception e) {
				log.error("Image upload failed for itemId={}, keeping old image", id);
			}
		}

		menuItemRepository.save(item);

		log.info("Menu item updated successfully. itemId={}", id);
	}

	@Override
	public void deleteMenuItem(Long id) {

		log.info("Deleting menu item. itemId={}", id);

		menuItemRepository.deleteById(id);

		log.info("Menu item deleted successfully. itemId={}", id);
	}

	@Override
	public void toggleAvailability(Long id) {

		log.info("Toggling availability for menu item. itemId={}", id);

		MenuItem item = menuItemRepository.findById(id).orElseThrow(() -> {
			log.error("Menu item not found with id={}", id);
			return new RuntimeException("Menu item not found");
		});

		item.setIsAvailable(!item.getIsAvailable());

		menuItemRepository.save(item);

		log.info("Menu item availability toggled. itemId={}, newStatus={}", id, item.getIsAvailable());
	}
}
