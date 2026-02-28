package com.app.fooddash.controller;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ================= CREATE RESTAURANT =================
    @PostMapping
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<Void>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {

        restaurantService.createRestaurant(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
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
    

}