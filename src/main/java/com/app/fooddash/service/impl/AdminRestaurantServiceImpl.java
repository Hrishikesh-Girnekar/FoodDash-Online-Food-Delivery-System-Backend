package com.app.fooddash.service.impl;

import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.entity.Restaurant;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.repository.RestaurantRepository;
import com.app.fooddash.service.AdminRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminRestaurantServiceImpl implements AdminRestaurantService {

	private final RestaurantRepository restaurantRepository;

	@Override
	public List<RestaurantResponse> getAllRestaurants() {

		log.info("Admin fetching all restaurants");

		List<RestaurantResponse> result = restaurantRepository.findAll().stream().map(this::mapToResponse).toList();

		log.info("Fetched {} restaurants", result.size());

		return result;
	}

	@Override
	public List<RestaurantResponse> getRestaurantsByStatus(RestaurantStatus status) {

		log.info("Admin fetching restaurants by status={}", status);

		List<RestaurantResponse> result = restaurantRepository.findByStatus(status).stream().map(this::mapToResponse)
				.toList();

		log.info("Fetched {} restaurants with status={}", result.size(), status);

		return result;
	}

	@Override
	public void updateRestaurantStatus(Long id, RestaurantStatus status) {

		log.info("Admin updating restaurant status. restaurantId={}, newStatus={}", id, status);

		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			log.error("Restaurant not found with id={}", id);
			return new RuntimeException("Restaurant not found");
		});

		if (restaurant.getStatus() != RestaurantStatus.PENDING) {
			log.warn("Invalid status update attempt. restaurantId={}, currentStatus={}, attemptedStatus={}", id,
					restaurant.getStatus(), status);
			throw new RuntimeException("Only pending restaurants can be approved");
		}

		restaurant.setStatus(status);
		restaurantRepository.save(restaurant);

		log.info("Restaurant status updated successfully. restaurantId={}, newStatus={}", id, status);
	}

	@Override
	public void deleteRestaurant(Long id) {

		log.info("Admin deleting restaurant. restaurantId={}", id);

		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			log.error("Restaurant not found with id={}", id);
			return new RuntimeException("Restaurant not found");
		});

		restaurantRepository.delete(restaurant);

		log.warn("Restaurant deleted by admin. restaurantId={}, name={}", id, restaurant.getName());
	}

	private RestaurantResponse mapToResponse(Restaurant restaurant) {

		return RestaurantResponse.builder().id(restaurant.getId()).name(restaurant.getName())
				.description(restaurant.getDescription()).phone(restaurant.getPhone()).cuisine(restaurant.getCuisine())
				.address(restaurant.getAddress()).city(restaurant.getCity()).openingTime(restaurant.getOpeningTime())
				.closingTime(restaurant.getClosingTime()).costForTwo(restaurant.getCostForTwo())
				.rating(restaurant.getRating()).totalReviews(restaurant.getTotalReviews())
				.status(restaurant.getStatus()).imageUrl(restaurant.getImageUrl()).createdAt(restaurant.getCreatedAt())
				.isOpen(restaurant.getIsOpen()).build();
	}
}