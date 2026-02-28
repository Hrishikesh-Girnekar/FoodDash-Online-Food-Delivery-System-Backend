package com.app.fooddash.dto.response;

import com.app.fooddash.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DeliveryOrderResponse {

    private Long orderId;

    private String restaurantName;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;

    private String customerName;
    
    private String customerPhone;
    
    private String customerAddress;
}