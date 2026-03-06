package com.app.fooddash.service;

import com.app.fooddash.dto.request.CreateOrderRequest;
import com.app.fooddash.dto.request.PaymentVerifyRequest;
import com.app.fooddash.dto.response.CreateOrderResponse;

public interface PaymentService {
	CreateOrderResponse createOrder(CreateOrderRequest request, String email);

	boolean verifyPayment(PaymentVerifyRequest request);
	

}
