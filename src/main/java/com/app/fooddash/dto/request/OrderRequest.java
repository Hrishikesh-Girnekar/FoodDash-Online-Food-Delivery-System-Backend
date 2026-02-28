package com.app.fooddash.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    private Long restaurantId;
    private List<OrderItemRequest> items;
}