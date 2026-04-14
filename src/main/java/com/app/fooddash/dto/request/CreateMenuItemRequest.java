package com.app.fooddash.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateMenuItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull
    private Boolean isVeg;

    private Boolean isBestseller;

//    private String imageUrl;

    private Boolean isAvailable;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}