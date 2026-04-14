package com.app.fooddash.controller;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.OwnerDashboardStatsResponse;
import com.app.fooddash.dto.response.RecentOrderResponse;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ================= CREATE RESTAURANT =================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<Void>> createRestaurant(
            @ModelAttribute CreateRestaurantRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {

        restaurantService.createRestaurant(request, image);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Restaurant created successfully. Awaiting admin approval.",
                        null
                )
        );
    }

    // ================= GET APPROVED RESTAURANTS =================
    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getApprovedRestaurants() {

        List<RestaurantResponse> data = restaurantService.getApprovedRestaurants();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Approved restaurants fetched successfully.",
                        data
                )
        );
    }
    
 // ================= GET RESTAURANT BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(
            @PathVariable Long id) {

        RestaurantResponse data = restaurantService.getRestaurantById(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant fetched successfully.",
                        data
                )
        );
    }
    
 // ================= GET MY RESTAURANTS =================
 	@GetMapping
 	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
 	public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getMyRestaurants() {

 		List<RestaurantResponse> data = restaurantService.getOwnerRestaurants();

 		return ResponseEntity.ok(new ApiResponse<>(true, "Owner restaurants fetched successfully.", data));
 	}
 	
 // ================= UPDATE RESTAURANT =================
 	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
 	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
 	public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
 	        @PathVariable Long id,
 	        @ModelAttribute @Valid CreateRestaurantRequest request,
 	        @RequestParam(value = "image", required = false) MultipartFile image
 	) {

 	    RestaurantResponse updated = restaurantService.updateRestaurant(id, request, image);

 	    return ResponseEntity.ok(
 	            new ApiResponse<>(true, "Restaurant updated successfully.", updated)
 	    );
 	}
 	
 // ================= DELETE RESTAURANT =================
 	@DeleteMapping("/{id}")
 	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
 	public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {

 		restaurantService.deleteRestaurant(id);

 		return ResponseEntity.ok(new ApiResponse<>(true, "Restaurant deleted successfully.", null));
 	}
 	
 // ================= TOGGLE OPEN/CLOSE =================
 	@PatchMapping("/{id}/toggle")
 	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
 	public ResponseEntity<ApiResponse<Void>> toggleRestaurant(@PathVariable Long id) {

 		restaurantService.toggleRestaurantStatus(id);

 		return ResponseEntity.ok(new ApiResponse<>(true, "Restaurant availability updated.", null));
 	}

 	// ================= DASHBOARD STATS =================
 	@GetMapping("/dashboard/stats")
 	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
 	public ResponseEntity<ApiResponse<OwnerDashboardStatsResponse>> getDashboardStats() {

 		OwnerDashboardStatsResponse stats = restaurantService.getOwnerDashboardStats();

 		return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard stats fetched successfully.", stats));
 	}

 	@GetMapping("/dashboard/recent-orders")
 	@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
 	public ResponseEntity<ApiResponse<List<RecentOrderResponse>>> getRecentOrders(
 	        Authentication authentication) {

 	    List<RecentOrderResponse> orders =
 	            restaurantService.getRecentOrders(authentication.getName());

 	    return ResponseEntity.ok(
 	            new ApiResponse<>(true, "Recent orders fetched successfully.", orders)
 	    );
 	}
    

}