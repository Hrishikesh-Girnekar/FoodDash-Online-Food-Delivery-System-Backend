package com.app.fooddash.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private String  orderId;    // e.g. "order_XXXXXXXXXX"
    private Integer amount;     // in paise (multiply rupees × 100)
    private String  currency;
    private String  keyId;      // public key — safe to expose
}
