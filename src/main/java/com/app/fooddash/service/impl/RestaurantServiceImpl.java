package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.OwnerDashboardStatsResponse;
import com.app.fooddash.dto.response.RecentOrderResponse;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.entity.Order;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.exception.UnauthorizedException;
import com.app.fooddash.mapper.RestaurantMapper;
import com.app.fooddash.mapper.OrderMapper; // ✅ FIXED
import com.app.fooddash.repository.OrderRepository;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.CloudinaryService;
import com.app.fooddash.service.RestaurantService;
import com.app.fooddash.util.AuthUtil;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

	private final RestaurantRepository restaurantRepository;
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final AuthUtil authUtil;
	private final CloudinaryService cloudinaryService;
	private final RestaurantMapper restaurantMapper;
	private final OrderMapper orderMapper; // ✅ works now

	@Override
	public void createRestaurant(CreateRestaurantRequest request, MultipartFile image) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Creating restaurant for user: {}", email);

		User owner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("User not found with email: {}", email);
			return new ResourceNotFoundException("User not found");
		});

		String imageUrl = null;

		if (image != null && !image.isEmpty()) {
			String publicId = "fooddash/restaurants/" + UUID.randomUUID();
			log.info("Uploading restaurant image to Cloudinary. publicId={}", publicId);

			imageUrl = cloudinaryService.uploadFile(image, "fooddash/restaurants", publicId);
		}

		Restaurant restaurant = restaurantMapper.toEntity(request);

		restaurant.setImageUrl(imageUrl);
		restaurant.setRating(4.2);
		restaurant.setTotalReviews(120);
		restaurant.setIsOpen(true);
		restaurant.setIsApproved(false);
		restaurant.setOwner(owner);

		restaurantRepository.save(restaurant);

		log.info("Restaurant created successfully for user: {}", email);
	}

	@Override
	public void approveRestaurant(Long restaurantId) {

		log.info("Approving restaurant with id: {}", restaurantId);

		Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> {
			log.error("Restaurant not found with id: {}", restaurantId);
			return new ResourceNotFoundException("Restaurant not found");
		});

		restaurant.setIsApproved(true);
		restaurantRepository.save(restaurant);

		log.info("Restaurant approved successfully: {}", restaurantId);
	}

	@Override
	public List<RestaurantResponse> getApprovedRestaurants() {

		log.info("Fetching approved restaurants");

		List<RestaurantResponse> result = restaurantRepository.findByStatus(RestaurantStatus.APPROVED).stream()
				.map(restaurantMapper::toResponse).toList();

		log.info("Fetched {} approved restaurants", result.size());
		return result;
	}

	@Override
	public List<RestaurantResponse> getAllRestaurants() {

		log.info("Fetching all restaurants");

		List<RestaurantResponse> result = restaurantRepository.findAll().stream().map(restaurantMapper::toResponse)
				.toList();

		log.info("Fetched {} restaurants", result.size());
		return result;
	}

	@Override
	public List<RestaurantResponse> getOwnerRestaurants() {

		User owner = authUtil.getCurrentUser();
		log.info("Fetching restaurants for ownerId={}", owner.getId());

		List<RestaurantResponse> result = restaurantRepository.findByOwner(owner).stream()
				.map(restaurantMapper::toResponse).toList();

		log.info("Fetched {} restaurants for ownerId={}", result.size(), owner.getId());
		return result;
	}

	@Override
	public void deleteRestaurant(Long id) {

		User owner = authUtil.getCurrentUser();
		log.info("Deleting restaurant id={} by ownerId={}", id, owner.getId());

		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			log.error("Restaurant not found with id: {}", id);
			return new RuntimeException("Restaurant not found");
		});

		if (!restaurant.getOwner().getId().equals(owner.getId())) {
			log.warn("Unauthorized delete attempt by ownerId={} on restaurantId={}", owner.getId(), id);
			throw new RuntimeException("You are not allowed to delete this restaurant");
		}

		restaurantRepository.delete(restaurant);

		log.info("Restaurant deleted successfully: {}", id);
	}

	@Override
	@Transactional
	public void toggleRestaurantStatus(Long id) {

		User owner = authUtil.getCurrentUser();
		log.info("Toggling restaurant status. restaurantId={}, ownerId={}", id, owner.getId());

		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			log.error("Restaurant not found with id: {}", id);
			return new RuntimeException("Restaurant not found");
		});

		if (!restaurant.getOwner().getId().equals(owner.getId())) {
			log.warn("Unauthorized status toggle attempt by ownerId={} on restaurantId={}", owner.getId(), id);
			throw new RuntimeException("You are not allowed to update this restaurant");
		}

		Boolean currentStatus = restaurant.getIsOpen();
		restaurant.setIsOpen(currentStatus == null ? true : !currentStatus);

		log.info("Restaurant status toggled. restaurantId={}, newStatus={}", id, restaurant.getIsOpen());
	}

	@Override
	public RestaurantResponse updateRestaurant(Long id, CreateRestaurantRequest request, MultipartFile image) {

		User owner = authUtil.getCurrentUser();
		log.info("Updating restaurant id={} by ownerId={}", id, owner.getId());

		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			log.error("Restaurant not found with id: {}", id);
			return new BadRequestException("Restaurant not found");
		});

		if (!restaurant.getOwner().getId().equals(owner.getId())) {
			log.warn("Unauthorized update attempt by ownerId={} on restaurantId={}", owner.getId(), id);
			throw new UnauthorizedException("You are not allowed to update this restaurant");
		}

		restaurantMapper.updateRestaurantFromDto(request, restaurant);

		if (image != null && !image.isEmpty()) {
			String publicId = "fooddash/restaurants/" + restaurant.getId();
			log.info("Updating restaurant image. restaurantId={}, publicId={}", id, publicId);

			String imageUrl = cloudinaryService.uploadFile(image, "fooddash/restaurants", publicId);
			restaurant.setImageUrl(imageUrl);
		}

		Restaurant saved = restaurantRepository.save(restaurant);

		log.info("Restaurant updated successfully: {}", id);

		return restaurantMapper.toUpdatedResponse(saved);
	}

	@Override
	public RestaurantResponse getRestaurantById(Long id) {

		log.info("Fetching restaurant by id: {}", id);

		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			log.error("Restaurant not found with id: {}", id);
			return new RuntimeException("Restaurant not found");
		});

		return restaurantMapper.toResponse(restaurant);
	}

	@Override
	public OwnerDashboardStatsResponse getOwnerDashboardStats() {

		User owner = authUtil.getCurrentUser();
		log.info("Fetching dashboard stats for ownerId={}", owner.getId());

		List<Restaurant> restaurants = restaurantRepository.findByOwner(owner);

		if (restaurants.isEmpty()) {
			log.warn("No restaurants found for ownerId={}", owner.getId());
			return restaurantMapper.toDashboardResponse(0L, 0L, 0.0, 0.0);
		}

		List<Long> restaurantIds = restaurants.stream().map(Restaurant::getId).toList();

		Long totalOrders = orderRepository.countByRestaurantIdIn(restaurantIds);

		LocalDate today = LocalDate.now();

		Long todayOrders = orderRepository.countByRestaurantIdInAndCreatedAtBetween(restaurantIds, today.atStartOfDay(),
				today.atTime(LocalTime.MAX));

		Double totalRevenue = orderRepository.sumRevenueByRestaurantIds(restaurantIds);
		if (totalRevenue == null)
			totalRevenue = 0.0;

		Double avgRating = restaurants.stream().map(Restaurant::getRating).filter(Objects::nonNull)
				.mapToDouble(Double::doubleValue).average().orElse(0.0);

		avgRating = Math.round(avgRating * 10.0) / 10.0;

		log.info("Dashboard stats calculated for ownerId={}", owner.getId());

		return restaurantMapper.toDashboardResponse(todayOrders, totalOrders, totalRevenue, avgRating);
	}

	@Override
	public List<RecentOrderResponse> getRecentOrders(String email) {

		log.info("Fetching recent orders for owner email={}", email);

		User owner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Owner not found with email: {}", email);
			return new RuntimeException("Owner not found");
		});

		List<Restaurant> restaurants = restaurantRepository.findByOwner(owner);

		if (restaurants.isEmpty()) {
			log.warn("No restaurants found for owner email={}", email);
			throw new RuntimeException("No restaurants found for this owner");
		}

		List<Order> orders = orderRepository.findTop3ByRestaurantInOrderByCreatedAtDesc(restaurants);

		log.info("Fetched {} recent orders for owner email={}", orders.size(), email);

		return orders.stream().map(orderMapper::toRecentOrderResponse).collect(Collectors.toList());
	}
}