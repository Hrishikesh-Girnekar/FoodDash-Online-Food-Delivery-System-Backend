package com.app.fooddash.service;

import java.util.List;

import com.app.fooddash.dto.request.OrderRequest;
import com.app.fooddash.dto.response.DeliveryOrderResponse;
import com.app.fooddash.dto.response.OrderResponse;
import com.app.fooddash.entity.Order;
import com.app.fooddash.enums.OrderStatus;

public interface OrderService {

	Order placeOrder(OrderRequest request);
    
    List<OrderResponse> getMyOrders();
    
    List<OrderResponse> getOrdersForOwner();
    
    void updateOrderStatus(Long orderId, OrderStatus status);
    
    List<OrderResponse> getAllOrdersForAdmin();
    
    void adminCancelOrder(Long orderId);
    
    void cancelMyOrder(Long orderId);
    
    void updateDeliveryStatus(Long orderId, OrderStatus status);
    
    void assignDeliveryPartner(Long orderId, Long deliveryPartnerId);
    
    void markAsDelivered(Long orderId);
    
    List<DeliveryOrderResponse> getAssignedOrdersForDeliveryPartner();
    
//    void verifyOtp(Long orderId, String enteredOtp);






}
