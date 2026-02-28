package com.app.fooddash.entity;

import com.app.fooddash.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "delivery_partner_id")
    private User deliveryPartner;
    
    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "delivery_address")
    private String deliveryAddress;
    
    @Column(name = "delivery_otp", length = 6)
    private String deliveryOtp;

    @Column(name = "otp_verified", nullable = false)
    private Boolean otpVerified = false;

    @Column(name = "otp_generated_time")
    private LocalDateTime otpGeneratedTime;

}
