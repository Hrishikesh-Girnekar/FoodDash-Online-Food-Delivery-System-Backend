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

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final RestaurantRepository restaurantRepository;
	private final MenuItemRepository menuItemRepository;

	@Override
	public Order placeOrder(OrderRequest request) {

		// 🔐 Get logged-in user
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

		Order order = new Order();
		order.setUser(user);
		order.setRestaurant(restaurant);
		order.setStatus(OrderStatus.PLACED);

		BigDecimal total = BigDecimal.ZERO;

		for (OrderItemRequest itemReq : request.getItems()) {

			MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

			BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

			total = total.add(itemTotal);

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setMenuItem(menuItem);
			orderItem.setQuantity(itemReq.getQuantity());
			orderItem.setPrice(menuItem.getPrice()); // snapshot price

			order.getItems().add(orderItem);
		}

		order.setTotalAmount(total);
		// 🔥 Delivery snapshot (for now use user data if available)
		order.setDeliveryPhone("9876543210");
		order.setDeliveryAddress("Pune, Hinjewadi Phase 1");

		return orderRepository.save(order);
	}

	@Override
	public List<OrderResponse> getMyOrders() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return orderRepository.findByUser(user).stream().map(order -> {

			var items = order.getItems().stream().map(item -> {

				var total = item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()));

				return new OrderItemResponse(item.getMenuItem().getName(), item.getPrice(), item.getQuantity(), total);
			}).toList();

			return new OrderResponse(order.getId(), order.getRestaurant().getName(), order.getStatus(),
					order.getTotalAmount(), order.getCreatedAt(), items);
		}).toList();
	}

	@Override
	public List<OrderResponse> getOrdersForOwner() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User owner = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		List<Restaurant> restaurants = restaurantRepository.findByOwner(owner);

		if (restaurants.isEmpty()) {
			throw new BadRequestException("No restaurants found for this owner");
		}

		List<Order> orders = restaurants.stream().flatMap(r -> orderRepository.findByRestaurant(r).stream())
				.sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())) // latest first
				.toList();

		return orders.stream().map(order -> {

			List<OrderItemResponse> items = order.getItems().stream()
					.map(item -> new OrderItemResponse(item.getMenuItem().getName(), item.getPrice(),
							item.getQuantity(),
							item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))))
					.toList();

			return new OrderResponse(order.getId(), order.getRestaurant().getName(), order.getStatus(),
					order.getTotalAmount(), order.getCreatedAt(), items);

		}).toList();
	}

	@Override
	@Transactional
	public void updateOrderStatus(Long orderId, OrderStatus newStatus) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User owner = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
			throw new UnauthorizedException("You cannot update this order");
		}

		OrderStatus currentStatus = order.getStatus();

		if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
			throw new BadRequestException("Order already completed");
		}

		// 🔥 STRICT TRANSITION RULES
		boolean validTransition = (currentStatus == OrderStatus.PLACED && newStatus == OrderStatus.ACCEPTED)
				|| (currentStatus == OrderStatus.ACCEPTED && newStatus == OrderStatus.PREPARING)
				|| (currentStatus == OrderStatus.PREPARING && newStatus == OrderStatus.OUT_FOR_DELIVERY);
//		     || (currentStatus == OrderStatus.OUT_FOR_DELIVERY && newStatus == OrderStatus.DELIVERED)

		if (!validTransition) {
			throw new BadRequestException("Invalid order status transition from " + currentStatus + " to " + newStatus);
		}

		order.setStatus(newStatus);
	}

//	@Override
//	@Transactional
//	public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
//
//		String email = SecurityContextHolder.getContext().getAuthentication().getName();
//
//		User owner = userRepository.findByEmail(email)
//				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
//
//		if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
//			throw new UnauthorizedException("You cannot update this order");
//		}
//
//		OrderStatus currentStatus = order.getStatus();
//
//		if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
//			throw new BadRequestException("Order already completed");
//		}
//
//		// 🔥 STRICT TRANSITION RULES
//		boolean validTransition = (currentStatus == OrderStatus.PLACED && newStatus == OrderStatus.ACCEPTED)
//				|| (currentStatus == OrderStatus.ACCEPTED && newStatus == OrderStatus.PREPARING)
//				|| (currentStatus == OrderStatus.PREPARING && newStatus == OrderStatus.OUT_FOR_DELIVERY);
//
//		if (!validTransition) {
//			throw new BadRequestException("Invalid order status transition from " + currentStatus + " to " + newStatus);
//		}
//
//		// ✅ If moving to OUT_FOR_DELIVERY → Auto assign partner + generate OTP
//		if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
//
//			if (order.getDeliveryPartner() == null) {
//
//				List<User> partners = userRepository.findByRoles_Name(RoleType.DELIVERY_PARTNER);
//
//				if (partners.isEmpty()) {
//					throw new BadRequestException("No delivery partners available");
//				}
//
//				User partner = partners.get(ThreadLocalRandom.current().nextInt(partners.size()));
//
//				order.setDeliveryPartner(partner);
//
//				String otp = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
//
//				order.setDeliveryOtp(otp);
//				order.setOtpVerified(false);
//				order.setOtpGeneratedTime(LocalDateTime.now());
//
//				System.out.println("Generated OTP for order " + orderId + " : " + otp);
//			}
//		}
//
//		order.setStatus(newStatus);
//	}

	@Override
	public List<OrderResponse> getAllOrdersForAdmin() {

		return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(order -> {

			var items = order.getItems().stream()
					.map(item -> new OrderItemResponse(item.getMenuItem().getName(), item.getPrice(),
							item.getQuantity(),
							item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))))
					.toList();

			return new OrderResponse(order.getId(), order.getRestaurant().getName(), order.getStatus(),
					order.getTotalAmount(), order.getCreatedAt(), items);
		}).toList();
	}

	@Override
	@Transactional
	public void adminCancelOrder(Long orderId) {

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (order.getStatus() == OrderStatus.DELIVERED) {
			throw new BadRequestException("Delivered orders cannot be cancelled");
		}

		order.setStatus(OrderStatus.CANCELLED);
	}

	@Override
	@Transactional
	public void cancelMyOrder(Long orderId) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (!order.getUser().getId().equals(user.getId())) {
			throw new UnauthorizedException("You cannot cancel this order");
		}

		if (order.getStatus() != OrderStatus.PLACED) {
			throw new BadRequestException("Order can only be cancelled while in PLACED status");
		}

		order.setStatus(OrderStatus.CANCELLED);
	}

//	@Override
//	@Transactional
//	public void assignDeliveryPartner(Long orderId, Long deliveryPartnerId) {
//
//		Order order = orderRepository.findById(orderId)
//				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
//
//		if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
//			throw new BadRequestException("Order must be OUT_FOR_DELIVERY before assigning delivery partner");
//		}
//
//		User deliveryPartner = userRepository.findById(deliveryPartnerId)
//				.orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
//
//		boolean isDeliveryRole = deliveryPartner.getRoles().stream()
//				.anyMatch(role -> role.getName() == RoleType.DELIVERY_PARTNER);
//
//		if (!isDeliveryRole) {
//			throw new BadRequestException("User is not a delivery partner");
//		}
//
//		order.setDeliveryPartner(deliveryPartner);
//	}
	@Override
	@Transactional
	public void assignDeliveryPartner(Long orderId, Long deliveryPartnerId) {

	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

	    if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
	        throw new BadRequestException("Order must be OUT_FOR_DELIVERY before assigning delivery partner");
	    }

	    User deliveryPartner = userRepository.findById(deliveryPartnerId)
	            .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));

	    order.setDeliveryPartner(deliveryPartner);
	    order.setStatus(OrderStatus.ASSIGNED);  // 🔥 ADD THIS
	    System.out.println(order.getStatus());
	}
//	@Transactional
//	public void assignDeliveryPartner(Long orderId) {
//
//		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
//
//		List<User> partners = userRepository.findByRoles_Name(RoleType.DELIVERY_PARTNER);
//
//		if (partners.isEmpty()) {
//			throw new RuntimeException("No delivery partners available");
//		}
//
//		// ✅ Random delivery partner selection (thread-safe & efficient)
//		User partner = partners.get(ThreadLocalRandom.current().nextInt(partners.size()));
//
//		order.setDeliveryPartner(partner);
//		order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
//
//		// Generate 4-digit OTP
//		String otp = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
//
//		order.setDeliveryOtp(otp);
//		order.setOtpVerified(false);
//		order.setOtpGeneratedTime(LocalDateTime.now());
//
//		orderRepository.save(order);
//
//		System.out.println("Generated OTP for Order " + orderId + ": " + otp);
//	}

	@Override
	@Transactional
	public void markAsDelivered(Long orderId) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User deliveryPartner = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
			throw new UnauthorizedException("You are not assigned to this order");
		}

		if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
			throw new BadRequestException("Order is not ready for delivery");
		}

		order.setStatus(OrderStatus.DELIVERED);
	}

	@Override
	public List<DeliveryOrderResponse> getAssignedOrdersForDeliveryPartner() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User deliveryPartner = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		List<Order> orders = orderRepository.findByDeliveryPartner(deliveryPartner);

		return orders.stream().map(order -> {

			var items = order.getItems().stream()
					.map(item -> new OrderItemResponse(item.getMenuItem().getName(), item.getPrice(),
							item.getQuantity(),
							item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))))
					.toList();

			return new DeliveryOrderResponse(order.getId(), order.getRestaurant().getName(), order.getStatus(),
					order.getTotalAmount(), order.getCreatedAt(), items, order.getUser().getFullName(),
					order.getDeliveryPhone(), order.getDeliveryAddress());

		}).toList();
	}

	@Override
	@Transactional
	public void updateDeliveryStatus(Long orderId, OrderStatus newStatus) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User deliveryPartner = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
			throw new UnauthorizedException("You are not assigned to this order");
		}

		OrderStatus currentStatus = order.getStatus();

		boolean validTransition = (currentStatus == OrderStatus.ASSIGNED && newStatus == OrderStatus.PICKED_UP)
				|| (currentStatus == OrderStatus.PICKED_UP && newStatus == OrderStatus.ON_THE_WAY)
				|| (currentStatus == OrderStatus.ON_THE_WAY && newStatus == OrderStatus.DELIVERED);

		if (!validTransition) {
			throw new BadRequestException("Invalid delivery status transition");
		}

		order.setStatus(newStatus);
	}

//	@Override
//	@Transactional
//	public void verifyOtp(Long orderId, String enteredOtp) {
//
//		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
//
//		if (order.getDeliveryOtp() == null) {
//			throw new RuntimeException("OTP not generated");
//		}
//
//		// OTP expiry check (10 minutes)
//		if (order.getOtpGeneratedTime().plusMinutes(10).isBefore(LocalDateTime.now())) {
//
//			throw new RuntimeException("OTP expired");
//		}
//
//		if (!order.getDeliveryOtp().equals(enteredOtp)) {
//			throw new RuntimeException("Invalid OTP");
//		}
//
//		order.setOtpVerified(true);
//		order.setStatus(OrderStatus.DELIVERED);
//
//		orderRepository.save(order);
//	}

	
}
