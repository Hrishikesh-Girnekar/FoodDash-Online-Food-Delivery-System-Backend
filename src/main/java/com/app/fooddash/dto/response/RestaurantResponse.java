package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.app.fooddash.enums.RestaurantStatus;

@Builder
@Getter
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private String name;
    private String description;
    private String phone;
    private String cuisine;
    private String address;
    private String city;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Integer costForTwo;
    private String imageUrl;
    private Double rating;
    private Integer totalReviews;
    private Boolean isOpen;
    private RestaurantStatus status;
    private LocalDateTime createdAt;
}