package com.app.fooddash.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.ApiResponse;
import com.app.fooddash.dto.response.MenuItemResponse;

public interface MenuService {

	void addMenuItem(CreateMenuItemRequest request, MultipartFile image);
    
    List<MenuItemResponse> getMenuByRestaurant(Long restaurantId);
    
    void updateMenuItem(Long menuItemId, CreateMenuItemRequest request, MultipartFile image);
    
    void deleteMenuItem(Long menuItemId);
    
    void toggleAvailability(Long menuItemId);

}
