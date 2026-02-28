package com.app.fooddash.service;

import com.app.fooddash.dto.request.AddToCartRequest;
import com.app.fooddash.dto.response.CartResponse;

public interface CartService {

    void addToCart(AddToCartRequest request);
    
    CartResponse viewCart();

}
