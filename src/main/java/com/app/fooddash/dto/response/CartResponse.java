package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private String restaurantName;
    private List<CartItemResponse> items;
    private BigDecimal cartTotal;
}
