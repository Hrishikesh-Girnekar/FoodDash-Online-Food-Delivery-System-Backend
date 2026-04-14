package com.app.fooddash.mapper;

import org.mapstruct.*;
import com.app.fooddash.dto.request.CreateMenuItemRequest;
import com.app.fooddash.dto.response.MenuItemResponse;
import com.app.fooddash.entity.MenuItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    // ================= CREATE =================
    @BeanMapping(ignoreByDefault = true)
    @Mappings({
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "price", source = "price"),
        @Mapping(target = "category", source = "category"),
        @Mapping(target = "isVeg", source = "isVeg"),
        @Mapping(target = "isBestseller", source = "isBestseller"),
        @Mapping(target = "isAvailable", source = "isAvailable")
    })
    MenuItem toEntity(CreateMenuItemRequest request);

    // ================= UPDATE =================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMenuItemFromDto(CreateMenuItemRequest request, @MappingTarget MenuItem item);

    // ================= RESPONSE =================
    MenuItemResponse toResponse(MenuItem item);

    List<MenuItemResponse> toResponseList(List<MenuItem> items);
}