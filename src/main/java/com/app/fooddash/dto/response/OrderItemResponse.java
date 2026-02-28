package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private String name;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal total;
}
