package com.app.fooddash.service;

import java.util.List;

import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.MenuItemResponse;

public interface MenuService {

    void addMenuItem(CreateMenuItemRequest request);
    
    List<MenuItemResponse> getMenuByRestaurant(Long restaurantId);
    
    void updateMenuItem(Long menuItemId, CreateMenuItemRequest request);
    
    void deleteMenuItem(Long menuItemId);
    
    void toggleAvailability(Long menuItemId);

}
