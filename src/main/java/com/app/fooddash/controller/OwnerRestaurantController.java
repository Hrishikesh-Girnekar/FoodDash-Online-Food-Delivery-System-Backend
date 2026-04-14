//package com.app.fooddash.controller;
//
//import com.app.fooddash.dto.request.CreateRestaurantRequest;
//import com.app.fooddash.dto.response.ApiResponse;
//import com.app.fooddash.dto.response.OwnerDashboardStatsResponse;
//import com.app.fooddash.dto.response.RecentOrderResponse;
//import com.app.fooddash.dto.response.RestaurantResponse;
//import com.app.fooddash.service.RestaurantService;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/owner/restaurants")
//@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
//public class OwnerRestaurantController {
//
//	private final RestaurantService restaurantService;
//
//	
//
//	// ================= CREATE RESTAURANT =================
////	@PostMapping
//	public ResponseEntity<ApiResponse<Void>> createRestaurant(
//            @ModelAttribute CreateRestaurantRequest request,
//            @RequestParam(value = "image", required = false) MultipartFile image
//    ) {
//
//        restaurantService.createRestaurant(request, image);
//
//        return ResponseEntity.ok(
//                ApiResponse.ok(
//                        "Restaurant created successfully. Awaiting admin approval.",
//                        null
//                )
//        );
//    }
//
//
//	// ================= TOGGLE OPEN/CLOSE =================
//	@PatchMapping("/{id}/toggle")
//	public ResponseEntity<ApiResponse<Void>> toggleRestaurant(@PathVariable Long id) {
//
//		restaurantService.toggleRestaurantStatus(id);
//
//		return ResponseEntity.ok(new ApiResponse<>(true, "Restaurant availability updated.", null));
//	}
//
//	// ================= DASHBOARD STATS =================
//	@GetMapping("/dashboard/stats")
//	public ResponseEntity<ApiResponse<OwnerDashboardStatsResponse>> getDashboardStats() {
//
//		OwnerDashboardStatsResponse stats = restaurantService.getOwnerDashboardStats();
//
//		return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard stats fetched successfully.", stats));
//	}
//
//	@GetMapping("/dashboard/recent-orders")
//	public ResponseEntity<ApiResponse<List<RecentOrderResponse>>> getRecentOrders(
//	        Authentication authentication) {
//
//	    List<RecentOrderResponse> orders =
//	            restaurantService.getRecentOrders(authentication.getName());
//
//	    return ResponseEntity.ok(
//	            new ApiResponse<>(true, "Recent orders fetched successfully.", orders)
//	    );
//	}
//}