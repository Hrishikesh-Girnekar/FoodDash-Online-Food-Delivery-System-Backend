package com.app.fooddash.controller;

import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.MenuItemResponse;
import com.app.fooddash.service.MenuService;
import com.app.fooddash.dto.response.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	// 1️⃣ ADD MENU ITEM
	@PostMapping
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> addMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {

		menuService.addMenuItem(request);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu item added successfully", null));
	}

	// 2️⃣ GET MENU BY RESTAURANT
	@GetMapping("/restaurant/{restaurantId}")
//    @PreAuthorize("hasAnyAuthority('CUSTOMER','ADMIN','RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(@PathVariable Long restaurantId) {

		List<MenuItemResponse> menu = menuService.getMenuByRestaurant(restaurantId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu fetched successfully", menu));
	}

	// 3️⃣ UPDATE MENU ITEM
	@PutMapping("/{menuItemId}")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> updateMenuItem(@PathVariable Long menuItemId,
			@Valid @RequestBody CreateMenuItemRequest request) {

		menuService.updateMenuItem(menuItemId, request);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu item updated successfully", null));
	}

	// 4️⃣ DELETE MENU ITEM
	@DeleteMapping("/{menuItemId}")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long menuItemId) {

		menuService.deleteMenuItem(menuItemId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu item deleted successfully", null));
	}

	// 5️⃣ TOGGLE AVAILABILITY
	@PatchMapping("/{menuItemId}/toggle")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> toggleAvailability(@PathVariable Long menuItemId) {

		menuService.toggleAvailability(menuItemId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Availability updated", null));
	}
}