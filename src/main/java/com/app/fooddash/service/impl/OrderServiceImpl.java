package com.app.fooddash.service.impl;

import com.app.fooddash.dto.request.OrderItemRequest;
import com.app.fooddash.dto.request.OrderRequest;
import com.app.fooddash.dto.response.DeliveryOrderResponse;
import com.app.fooddash.dto.response.OrderItemResponse;
import com.app.fooddash.dto.response.OrderResponse;
import com.app.fooddash.entity.*;
import com.app.fooddash.enums.OrderStatus;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.exception.BadRequestException;
import com.app.fooddash.exception.ResourceNotFoundException;
import com.app.fooddash.exception.UnauthorizedException;
import com.app.fooddash.mapper.OrderMapper;
import com.app.fooddash.repository.*;
import com.app.fooddash.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final RestaurantRepository restaurantRepository;
	private final MenuItemRepository menuItemRepository;
	private final OrderMapper orderMapper;

	@Override
	public Order placeOrder(OrderRequest request) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Placing order for user={}", email);

		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("User not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElseThrow(() -> {
			log.error("Restaurant not found with id={}", request.getRestaurantId());
			return new ResourceNotFoundException("Restaurant not found");
		});

		Order order = new Order();
		order.setUser(user);
		order.setRestaurant(restaurant);
		order.setStatus(OrderStatus.PLACED);

		BigDecimal total = BigDecimal.ZERO;

		for (OrderItemRequest itemReq : request.getItems()) {

			MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId()).orElseThrow(() -> {
				log.error("Menu item not found with id={}", itemReq.getMenuItemId());
				return new ResourceNotFoundException("Menu item not found");
			});

			BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
			total = total.add(itemTotal);

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setMenuItem(menuItem);
			orderItem.setQuantity(itemReq.getQuantity());
			orderItem.setPrice(menuItem.getPrice());

			order.getItems().add(orderItem);
		}

		order.setTotalAmount(total);
		order.setDeliveryPhone("9876543210");
		order.setDeliveryAddress("Pune, Hinjewadi Phase 1");

		Order savedOrder = orderRepository.save(order);

		log.info("Order placed successfully. orderId={}, user={}", savedOrder.getId(), email);

		return savedOrder;
	}

	@Override
	public List<OrderResponse> getMyOrders() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Fetching orders for user={}", email);

		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("User not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		List<OrderResponse> result = orderRepository.findByUser(user).stream().map(orderMapper::toOrderResponse)
				.toList();

		log.info("Fetched {} orders for user={}", result.size(), email);
		return result;
	}

	@Override
	public List<OrderResponse> getOrdersForOwner() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Fetching orders for owner={}", email);

		User owner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Owner not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		List<Restaurant> restaurants = restaurantRepository.findByOwner(owner);

		if (restaurants.isEmpty()) {
			log.warn("No restaurants found for owner={}", email);
			throw new BadRequestException("No restaurants found for this owner");
		}

		List<Order> orders = restaurants.stream().flatMap(r -> orderRepository.findByRestaurant(r).stream())
				.sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())).toList();

		log.info("Fetched {} orders for owner={}", orders.size(), email);

		return orders.stream().map(orderMapper::toOrderResponse).toList();
	}

	@Override
	@Transactional
	public void updateOrderStatus(Long orderId, OrderStatus newStatus) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Updating order status. orderId={}, newStatus={}, owner={}", orderId, newStatus, email);

		User owner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Owner not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found with id={}", orderId);
			return new ResourceNotFoundException("Order not found");
		});

		if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
			log.warn("Unauthorized order status update attempt. orderId={}, owner={}", orderId, email);
			throw new UnauthorizedException("You cannot update this order");
		}

		OrderStatus currentStatus = order.getStatus();

		if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
			log.warn("Order already completed. orderId={}, status={}", orderId, currentStatus);
			throw new BadRequestException("Order already completed");
		}

		boolean validTransition = (currentStatus == OrderStatus.PLACED && newStatus == OrderStatus.ACCEPTED)
				|| (currentStatus == OrderStatus.ACCEPTED && newStatus == OrderStatus.PREPARING)
				|| (currentStatus == OrderStatus.PREPARING && newStatus == OrderStatus.OUT_FOR_DELIVERY);

		if (!validTransition) {
			log.warn("Invalid order status transition. orderId={}, from={}, to={}", orderId, currentStatus, newStatus);
			throw new BadRequestException("Invalid order status transition from " + currentStatus + " to " + newStatus);
		}

		order.setStatus(newStatus);

		log.info("Order status updated successfully. orderId={}, newStatus={}", orderId, newStatus);
	}

	@Override
	public List<OrderResponse> getAllOrdersForAdmin() {

		log.info("Fetching all orders for admin");

		List<OrderResponse> result = orderRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(orderMapper::toOrderResponse).toList();

		log.info("Fetched {} orders for admin", result.size());
		return result;
	}

	@Override
	@Transactional
	public void adminCancelOrder(Long orderId) {

		log.info("Admin cancelling order. orderId={}", orderId);

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found with id={}", orderId);
			return new ResourceNotFoundException("Order not found");
		});

		if (order.getStatus() == OrderStatus.DELIVERED) {
			log.warn("Attempt to cancel delivered order. orderId={}", orderId);
			throw new BadRequestException("Delivered orders cannot be cancelled");
		}

		order.setStatus(OrderStatus.CANCELLED);

		log.info("Order cancelled by admin. orderId={}", orderId);
	}

	@Override
	@Transactional
	public void cancelMyOrder(Long orderId) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User cancelling order. orderId={}, user={}", orderId, email);

		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("User not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found with id={}", orderId);
			return new ResourceNotFoundException("Order not found");
		});

		if (!order.getUser().getId().equals(user.getId())) {
			log.warn("Unauthorized cancel attempt. orderId={}, user={}", orderId, email);
			throw new UnauthorizedException("You cannot cancel this order");
		}

		if (order.getStatus() != OrderStatus.PLACED) {
			log.warn("Invalid cancel attempt. orderId={}, status={}", orderId, order.getStatus());
			throw new BadRequestException("Order can only be cancelled while in PLACED status");
		}

		order.setStatus(OrderStatus.CANCELLED);

		log.info("Order cancelled by user. orderId={}", orderId);
	}

	@Override
	@Transactional
	public void assignDeliveryPartner(Long orderId, Long deliveryPartnerId) {

		log.info("Assigning delivery partner. orderId={}, deliveryPartnerId={}", orderId, deliveryPartnerId);

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found with id={}", orderId);
			return new ResourceNotFoundException("Order not found");
		});

		if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
			log.warn("Invalid state for assignment. orderId={}, status={}", orderId, order.getStatus());
			throw new BadRequestException("Order must be OUT_FOR_DELIVERY before assigning delivery partner");
		}

		User deliveryPartner = userRepository.findById(deliveryPartnerId).orElseThrow(() -> {
			log.error("Delivery partner not found with id={}", deliveryPartnerId);
			return new ResourceNotFoundException("Delivery partner not found");
		});

		order.setDeliveryPartner(deliveryPartner);
		order.setStatus(OrderStatus.ASSIGNED);

		log.info("Delivery partner assigned successfully. orderId={}, deliveryPartnerId={}", orderId,
				deliveryPartnerId);
	}

	@Override
	@Transactional
	public void markAsDelivered(Long orderId) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Marking order as delivered. orderId={}, deliveryPartner={}", orderId, email);

		User deliveryPartner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Delivery partner not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found with id={}", orderId);
			return new ResourceNotFoundException("Order not found");
		});

		if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
			log.warn("Unauthorized delivery attempt. orderId={}, user={}", orderId, email);
			throw new UnauthorizedException("You are not assigned to this order");
		}

		if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
			log.warn("Invalid delivery state. orderId={}, status={}", orderId, order.getStatus());
			throw new BadRequestException("Order is not ready for delivery");
		}

		order.setStatus(OrderStatus.DELIVERED);

		log.info("Order marked as delivered. orderId={}", orderId);
	}

	@Override
	public List<DeliveryOrderResponse> getAssignedOrdersForDeliveryPartner() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Fetching assigned orders for deliveryPartner={}", email);

		User deliveryPartner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Delivery partner not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		List<Order> orders = orderRepository.findByDeliveryPartner(deliveryPartner);

		log.info("Fetched {} assigned orders for deliveryPartner={}", orders.size(), email);

		return orderMapper.toDeliveryOrderResponseList(orders);
	}

	@Override
	@Transactional
	public void updateDeliveryStatus(Long orderId, OrderStatus newStatus) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Updating delivery status. orderId={}, newStatus={}, deliveryPartner={}", orderId, newStatus, email);

		User deliveryPartner = userRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Delivery partner not found with email={}", email);
			return new ResourceNotFoundException("User not found");
		});

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found with id={}", orderId);
			return new ResourceNotFoundException("Order not found");
		});

		if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
			log.warn("Unauthorized delivery status update. orderId={}, user={}", orderId, email);
			throw new UnauthorizedException("You are not assigned to this order");
		}

		OrderStatus currentStatus = order.getStatus();

		boolean validTransition = (currentStatus == OrderStatus.ASSIGNED && newStatus == OrderStatus.PICKED_UP)
				|| (currentStatus == OrderStatus.PICKED_UP && newStatus == OrderStatus.ON_THE_WAY)
				|| (currentStatus == OrderStatus.ON_THE_WAY && newStatus == OrderStatus.DELIVERED);

		if (!validTransition) {
			log.warn("Invalid delivery status transition. orderId={}, from={}, to={}", orderId, currentStatus,
					newStatus);
			throw new BadRequestException("Invalid delivery status transition");
		}

		order.setStatus(newStatus);

		log.info("Delivery status updated successfully. orderId={}, newStatus={}", orderId, newStatus);
	}

}
