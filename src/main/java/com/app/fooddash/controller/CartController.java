package com.app.fooddash.controller;

import com.app.fooddash.dto.request.AddToCartRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.CartResponse;
import com.app.fooddash.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@PostMapping("/add")
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<ApiResponse<Void>> addToCart(@Valid @RequestBody AddToCartRequest request) {
		cartService.addToCart(request);
		return ResponseEntity.ok(new ApiResponse<>(true, "Item added to cart", null));

	}

	@GetMapping
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<ApiResponse<CartResponse>> viewCart() {

		CartResponse data = cartService.viewCart();

		return ResponseEntity.ok(new ApiResponse<>(true, "Cart fetched successfully", data));
	}
}
