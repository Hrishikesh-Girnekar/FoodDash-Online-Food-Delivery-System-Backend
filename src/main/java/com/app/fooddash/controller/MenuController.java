package com.app.fooddash.controller;

import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.MenuItemResponse;
import com.app.fooddash.service.MenuService;
import com.app.fooddash.dto.response.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	// ADD MENU ITEM
	@PostMapping
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<?>> addMenuItem(@Valid @ModelAttribute CreateMenuItemRequest request, @RequestParam(value = "image", required = false) MultipartFile image) {

		menuService.addMenuItem(request, image);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu item added successfully", null));
	}

	// GET MENU BY RESTAURANT
	@GetMapping("/restaurant/{restaurantId}")
//    @PreAuthorize("hasAnyAuthority('CUSTOMER','ADMIN','RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(@PathVariable Long restaurantId) {

		List<MenuItemResponse> menu = menuService.getMenuByRestaurant(restaurantId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu fetched successfully", menu));
	}

	// UPDATE MENU ITEM
	@PutMapping(value = "/{menuItemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> updateMenuItem(
	        @PathVariable Long menuItemId,
	        @ModelAttribute CreateMenuItemRequest request,
	        @RequestPart(value = "image", required = false) MultipartFile image) {

	    menuService.updateMenuItem(menuItemId, request, image);

	    return ResponseEntity.ok(new ApiResponse<>(true, "Menu item updated successfully", null));
	}

	// DELETE MENU ITEM
	@DeleteMapping("/{menuItemId}")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long menuItemId) {

		menuService.deleteMenuItem(menuItemId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Menu item deleted successfully", null));
	}

	// TOGGLE AVAILABILITY
	@PatchMapping("/{menuItemId}/toggle")
	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
	public ResponseEntity<ApiResponse<Void>> toggleAvailability(@PathVariable Long menuItemId) {

		menuService.toggleAvailability(menuItemId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Availability updated", null));
	}
}