package com.app.fooddash.mapper;

import org.mapstruct.*;
import com.app.fooddash.dto.response.*;
import com.app.fooddash.entity.Order;
import com.app.fooddash.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ================= ORDER ITEM =================

    @Mapping(target = "name", source = "menuItem.name")
    @Mapping(target = "total", expression = "java(calculateTotal(item))")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    default BigDecimal calculateTotal(OrderItem item) {
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    // ================= USER ORDER =================
    @Mapping(source = "id", target = "orderId")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "items", source = "items")
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponseList(List<Order> orders);

 // ================= DELIVERY ORDER =================

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "customerName", source = "user.fullName")
    @Mapping(target = "customerPhone", source = "deliveryPhone")
    @Mapping(target = "customerAddress", source = "deliveryAddress")
    DeliveryOrderResponse toDeliveryOrderResponse(Order order);

    // ✅ VERY IMPORTANT
    List<DeliveryOrderResponse> toDeliveryOrderResponseList(List<Order> orders);

    @Mapping(target = "customerName", source = "user.fullName")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "items", expression = "java(buildItems(order))")
    RecentOrderResponse toRecentOrderResponse(Order order);

    default String buildItems(Order order) {
        return order.getItems().stream()
                .map(item -> item.getMenuItem().getName() + " x" + item.getQuantity())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}