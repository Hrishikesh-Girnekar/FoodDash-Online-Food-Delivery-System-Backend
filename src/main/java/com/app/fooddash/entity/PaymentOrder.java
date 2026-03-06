package com.app.fooddash.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razorpay_order_id", unique = true, nullable = false)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(nullable = false)
    private Integer amount;

    private String currency;

    private String receipt;

    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

//    @Column(name = "user_id")
//    private String userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum PaymentStatus {
        CREATED,
        PAID,
        FAILED
    }
}
