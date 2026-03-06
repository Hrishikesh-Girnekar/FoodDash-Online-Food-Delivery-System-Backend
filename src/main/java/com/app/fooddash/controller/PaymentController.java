package com.app.fooddash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.app.fooddash.dto.request.CreateOrderRequest;
import com.app.fooddash.dto.request.PaymentVerifyRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.CreateOrderResponse;
import com.app.fooddash.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/payment/create-order
     *
     * Creates a Razorpay order. Requires authenticated user.
     * Frontend calls this first, then opens the Razorpay checkout modal.
     *
     * @body CreateOrderRequest { amount (rupees), currency?, receipt? }
     * @return CreateOrderResponse { orderId, amount, currency, keyId }
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails user) {

        log.info("Create order request: amount=₹{} by user={}",
                 request.getAmount(), user.getUsername());

        CreateOrderResponse response = paymentService.createOrder(request, user.getUsername());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Order created successfully", response));
    }

    /**
     * POST /api/payment/verify
     *
     * Verifies the HMAC-SHA256 signature from Razorpay. 
     * Called AFTER the user completes payment in the Razorpay modal.
     *
     * @body PaymentVerifyRequest {
     *           razorpayOrderId, razorpayPaymentId, razorpaySignature
     *       }
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {

        boolean verified = paymentService.verifyPayment(request);

        if (verified) {
            return ResponseEntity.ok(
                ApiResponse.ok("Payment verified successfully", null)
            );
        } else {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Payment verification failed. Signature mismatch."));
        }
    }
}