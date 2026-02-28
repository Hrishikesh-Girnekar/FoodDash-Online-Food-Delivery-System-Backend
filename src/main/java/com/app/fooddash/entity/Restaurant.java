package com.app.fooddash.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

import com.app.fooddash.enums.RestaurantStatus;

@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= BASIC INFO =================
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String cuisine;

    @Column(name = "cost_for_two")
    private Integer costForTwo;

    // ================= LOCATION =================
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    // ================= TIME =================
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    // ================= DISPLAY FIELDS =================
    @Column(name = "image_url")
    private String imageUrl;

    private Double rating = 4.2;  // 🔥 Hardcoded default for now

    @Column(name = "total_reviews")
    private Integer totalReviews = 1200; // Hardcoded demo

    @Column(name = "is_open")
    private Boolean isOpen = true;

    @Column(name = "is_approved")
    private Boolean isApproved = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status = RestaurantStatus.PENDING;

    // ================= FK =================
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}