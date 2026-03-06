package com.app.fooddash.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequest {

    // These three values come from Razorpay's frontend callback
    @NotBlank private String razorpayOrderId;
    @NotBlank private String razorpayPaymentId;
    @NotBlank private String razorpaySignature;
}