package com.app.fooddash.service.impl;

import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fooddash.dto.request.CreateOrderRequest;
import com.app.fooddash.dto.request.PaymentVerifyRequest;
import com.app.fooddash.dto.response.CreateOrderResponse;
import com.app.fooddash.entity.PaymentOrder;
import com.app.fooddash.entity.PaymentOrder.PaymentStatus;
import com.app.fooddash.entity.User;
import com.app.fooddash.exception.PaymentException;
import com.app.fooddash.repository.OrderRepository;
import com.app.fooddash.repository.PaymentOrderRepository;
import com.app.fooddash.repository.UserRepository;
import com.app.fooddash.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final RazorpayClient razorpayClient;
	private final PaymentOrderRepository orderRepo;
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;

	@Value("${razorpay.key.id}")
	private String keyId;

	@Value("${razorpay.key.secret}")
	private String keySecret;

	@Override
	@Transactional
	public CreateOrderResponse createOrder(CreateOrderRequest request, String email) {
		try {

			// ① Build Razorpay options
			JSONObject options = new JSONObject();
			options.put("amount", request.getAmount() * 100); // rupees → paise
			options.put("currency", request.getCurrency());
			options.put("receipt",
					request.getReceipt() != null ? request.getReceipt() : "receipt_" + System.currentTimeMillis());
			options.put("payment_capture", 1);

			// ② Call Razorpay API
			Order razorOrder = razorpayClient.orders.create(options);
//			log.info("Razorpay order created: {}", String.valueOf(razorOrder.get("id")));

			// ③ Fetch logged-in user
			User user = userRepository.findByEmail(email).orElseThrow(() -> new PaymentException("User not found"));

			// ④ Save order in DB
			PaymentOrder entity = PaymentOrder.builder().razorpayOrderId((String) razorOrder.get("id"))
					.amount(request.getAmount() * 100).currency(request.getCurrency())
					.receipt((String) razorOrder.get("receipt")).status(PaymentStatus.CREATED).user(user) // 🔗
																											// important
					.build();

			orderRepo.save(entity);

			// ⑤ Return response to frontend
			return CreateOrderResponse.builder().orderId((String) razorOrder.get("id"))
					.amount((Integer) razorOrder.get("amount")).currency((String) razorOrder.get("currency"))
					.keyId(keyId).build();

		} catch (RazorpayException e) {
			log.error("Failed to create Razorpay order: {}", e.getMessage());
			throw new PaymentException("Order creation failed: " + e.getMessage());
		}
	}


	// ─── VERIFY PAYMENT ────────────────────────────────────────────
	@Override
	@Transactional
	public boolean verifyPayment(PaymentVerifyRequest request) {
		try {
			// ① Prevent duplicate processing (idempotency check)
			if (orderRepo.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
				log.warn("Duplicate payment attempt: {}", request.getRazorpayPaymentId());
				return true; // Already processed — return success silently
			}

			// ② Reconstruct the signature payload
			String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

			// ③ Compute HMAC-SHA256 with your secret key
			String expectedSignature = computeHmacSha256(payload, keySecret);

			// ④ Constant-time comparison to prevent timing attacks
			boolean isValid = MessageDigest.isEqual(expectedSignature.getBytes(),
					request.getRazorpaySignature().getBytes());

			if (!isValid) {
				log.error("Signature mismatch! Possible payment tampering for order: {}", request.getRazorpayOrderId());
				return false;
			}

			// ⑤ Update our DB — mark as PAID
			PaymentOrder order = orderRepo.findByRazorpayOrderId(request.getRazorpayOrderId())
					.orElseThrow(() -> new PaymentException("Order not found in DB"));

			order.setRazorpayPaymentId(request.getRazorpayPaymentId());
			order.setStatus(PaymentStatus.PAID);
			orderRepo.save(order);

			log.info("Payment verified and recorded. OrderId={}, PaymentId={}", request.getRazorpayOrderId(),
					request.getRazorpayPaymentId());

			return true;

		} catch (Exception e) {
			log.error("Payment verification error: {}", e.getMessage());
			throw new PaymentException("Verification failed: " + e.getMessage());
		}
	}

	// ─── HMAC-SHA256 HELPER ────────────────────────────────────────
	/**
	 * Computes HMAC-SHA256 signature. This replicates exactly what Razorpay signs
	 * with your secret key.
	 */
	private String computeHmacSha256(String data, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
		byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
		return HexFormat.of().formatHex(hash); // Java 17+
	}
}