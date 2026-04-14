package com.app.fooddash.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderResponse {

    private Long id;
    private String customerName;
    private String items;
    private String status;
    private BigDecimal totalAmount; 
}