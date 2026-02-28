package com.app.fooddash.controller;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.service.RestaurantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
public class OwnerRestaurantController {

    private final RestaurantService restaurantService;

    // ================= GET MY RESTAURANTS =================
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getMyRestaurants() {

        List<RestaurantResponse> data = restaurantService.getOwnerRestaurants();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Owner restaurants fetched successfully.", data)
        );
    }

    // ================= CREATE RESTAURANT =================
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {

        restaurantService.createRestaurant(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurant created successfully.", null)
        );
    }

    // ================= UPDATE RESTAURANT =================
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody CreateRestaurantRequest request) {

        restaurantService.updateRestaurant(id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurant updated successfully.", null)
        );
    }

    // ================= DELETE RESTAURANT =================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {

        restaurantService.deleteRestaurant(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurant deleted successfully.", null)
        );
    }

    // ================= TOGGLE OPEN/CLOSE =================
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleRestaurant(@PathVariable Long id) {

        restaurantService.toggleRestaurantStatus(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurant availability updated.", null)
        );
    }
}