package com.app.fooddash.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    private Long menuItemId;
    private Integer quantity;
}