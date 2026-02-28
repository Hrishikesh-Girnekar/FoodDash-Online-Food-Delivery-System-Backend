package com.app.fooddash.controller;

import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.enums.RestaurantStatus;
import com.app.fooddash.service.AdminRestaurantService;
import com.app.fooddash.service.RestaurantService;


import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Builder
@RestController
@RequestMapping("/api/v1/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminRestaurantController {

    private final AdminRestaurantService adminRestaurantService;
    private final RestaurantService restaurantService;

    // ================= GET ALL RESTAURANTS =================
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {

        List<RestaurantResponse> data = adminRestaurantService.getAllRestaurants();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "All restaurants fetched successfully.",
                        data
                )
        );
    }

    // ================= GET RESTAURANTS BY STATUS =================
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurantsByStatus(
            @PathVariable RestaurantStatus status) {

        List<RestaurantResponse> data =
                adminRestaurantService.getRestaurantsByStatus(status);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurants fetched successfully.",
                        data
                )
        );
    }

    // ================= UPDATE RESTAURANT STATUS =================
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateRestaurantStatus(
            @PathVariable Long id,
            @RequestParam RestaurantStatus status) {

        adminRestaurantService.updateRestaurantStatus(id, status);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant status updated successfully.",
                        null
                )
        );
    }

    // ================= DELETE RESTAURANT =================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable Long id) {

        adminRestaurantService.deleteRestaurant(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurant deleted successfully.",
                        null
                )
        );
    }
    
   
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<ApiResponse<Void>> approveRestaurant(
//            @PathVariable Long id) {
//
//        adminRestaurantService.updateRestaurantStatus(id, RestaurantStatus.APPROVED);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Restaurant approved successfully.",
//                        null
//                )
//        );
//    }
}