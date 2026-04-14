package com.app.fooddash.controller;

import com.app.fooddash.dto.request.OrderRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.DeliveryOrderResponse;
import com.app.fooddash.dto.response.OrderResponse;
import com.app.fooddash.dto.response.UserResponse;
import com.app.fooddash.entity.Order;
import com.app.fooddash.entity.User;
import com.app.fooddash.enums.OrderStatus;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final UserRepository userRepository;

	@PostMapping("/place")
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<ApiResponse<Long>> placeOrder(@RequestBody OrderRequest request) {

		Order order = orderService.placeOrder(request);

		return ResponseEntity.ok(new ApiResponse<>(true, "Order placed successfully", order.getId()));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<List<OrderResponse>> getMyOrders() {

		return ResponseEntity.ok(orderService.getMyOrders());
	}

	@GetMapping("/owner")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersForOwner() {

		List<OrderResponse> orders = orderService.getOrdersForOwner();

		return ResponseEntity.ok(new ApiResponse<>(true, "Orders fetched successfully", orders));
	}

	@PutMapping("/{orderId}/status")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> updateOrderStatus(@PathVariable Long orderId,
			@RequestParam OrderStatus status) {
		orderService.updateOrderStatus(orderId, status);
		return ResponseEntity.ok(new ApiResponse<>(true, "Order status updated", null));
	}

	@GetMapping("/admin")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<List<OrderResponse>> getAllOrdersForAdmin() {
		return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
	}

	@PutMapping("/admin/{orderId}/cancel")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> adminCancelOrder(@PathVariable Long orderId) {
		orderService.adminCancelOrder(orderId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Order cancelled by admin", null));
	}

	@PutMapping("/{orderId}/cancel")
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<ApiResponse<Void>> cancelMyOrder(@PathVariable Long orderId) {
		orderService.cancelMyOrder(orderId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Order cancelled successfully", null));
	}

	@PutMapping("/admin/{orderId}/assign/{partnerId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> assignDeliveryPartner(@PathVariable Long orderId,
			@PathVariable Long partnerId) {

		orderService.assignDeliveryPartner(orderId, partnerId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Delivery partner assigned successfully", null));

	}

	@PutMapping("/delivery/{orderId}/deliver")
	@PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
	public ResponseEntity<ApiResponse<Void>> markDelivered(@PathVariable Long orderId) {

		orderService.markAsDelivered(orderId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Order marked as delivered", null));
	}

	@GetMapping("/delivery")
	@PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
	public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> getAssignedOrders() {

		List<DeliveryOrderResponse> data = orderService.getAssignedOrdersForDeliveryPartner();

		return ResponseEntity.ok(new ApiResponse<>(true, "Assigned orders fetched successfully", data));
	}

	@PutMapping("/delivery/{orderId}/status")
	@PreAuthorize("hasAuthority('DELIVERY_PARTNER')")
	public ResponseEntity<ApiResponse<Void>> updateDeliveryStatus(@PathVariable Long orderId,
			@RequestParam OrderStatus status) {

		orderService.updateDeliveryStatus(orderId, status);

		return ResponseEntity.ok(new ApiResponse<>(true, "Delivery status updated", null));
	}

	@GetMapping("/admin/delivery-partners")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<ApiResponse<List<UserResponse>>> getDeliveryPartners() {

		List<User> partners = userRepository.findByRoles_Name(RoleType.DELIVERY_PARTNER);

		List<UserResponse> data = partners.stream()
				.map(user -> new UserResponse(user.getId(), user.getFullName(), user.getEmail())).toList();

		return ResponseEntity.ok(new ApiResponse<>(true, "Delivery partners fetched successfully", data));
	}

}
