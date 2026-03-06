package com.app.fooddash.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.fooddash.entity.PaymentOrder;

import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);

    // Used to prevent duplicate fulfillment
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}
