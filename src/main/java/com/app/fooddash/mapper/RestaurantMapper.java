package com.app.fooddash.mapper;

import org.mapstruct.*;

import com.app.fooddash.dto.request.CreateRestaurantRequest;
import com.app.fooddash.dto.response.OwnerDashboardStatsResponse;
import com.app.fooddash.dto.response.RecentOrderResponse;
import com.app.fooddash.dto.response.RestaurantResponse;
import com.app.fooddash.entity.Order;
import com.app.fooddash.entity.Restaurant;
//ADD THIS IMPORT
import com.app.fooddash.dto.response.RestaurantSummaryDto;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

	// CREATE
	@BeanMapping(ignoreByDefault = true)
	@Mappings({ @Mapping(target = "name", source = "name"), @Mapping(target = "description", source = "description"),
			@Mapping(target = "phone", source = "phone"), @Mapping(target = "cuisine", source = "cuisine"),
			@Mapping(target = "address", source = "address"), @Mapping(target = "city", source = "city"),
			@Mapping(target = "openingTime", source = "openingTime"),
			@Mapping(target = "closingTime", source = "closingTime"),
			@Mapping(target = "costForTwo", source = "costForTwo") })
	Restaurant toEntity(CreateRestaurantRequest request);

	// UPDATE (ignore null values)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
	@Mappings({ @Mapping(target = "name", source = "name"), @Mapping(target = "description", source = "description"),
			@Mapping(target = "phone", source = "phone"), @Mapping(target = "cuisine", source = "cuisine"),
			@Mapping(target = "address", source = "address"), @Mapping(target = "city", source = "city"),
			@Mapping(target = "openingTime", source = "openingTime"),
			@Mapping(target = "closingTime", source = "closingTime"),
			@Mapping(target = "costForTwo", source = "costForTwo") })
	void updateRestaurantFromDto(CreateRestaurantRequest request, @MappingTarget Restaurant restaurant);

	// ENTITY → RESPONSE (FULL)
	RestaurantResponse toResponse(Restaurant restaurant);

	// ENTITY → RESPONSE (UPDATED RESPONSE)
	@BeanMapping(ignoreByDefault = true)
	@Mappings({ @Mapping(target = "id", source = "id"), @Mapping(target = "name", source = "name"),
			@Mapping(target = "description", source = "description"), @Mapping(target = "cuisine", source = "cuisine"),
			@Mapping(target = "phone", source = "phone"), @Mapping(target = "address", source = "address"),
			@Mapping(target = "city", source = "city"), @Mapping(target = "openingTime", source = "openingTime"),
			@Mapping(target = "closingTime", source = "closingTime"),
			@Mapping(target = "costForTwo", source = "costForTwo"),
			@Mapping(target = "imageUrl", source = "imageUrl") })
	RestaurantResponse toUpdatedResponse(Restaurant restaurant);

	// OWNER DASHBOARD (simple direct mapping)
	@Mappings({ @Mapping(target = "todayOrders", source = "todayOrders"),
			@Mapping(target = "totalOrders", source = "totalOrders"),
			@Mapping(target = "totalRevenue", source = "totalRevenue"),
			@Mapping(target = "averageRating", source = "avgRating") })
	OwnerDashboardStatsResponse toDashboardResponse(Long todayOrders, Long totalOrders, Double totalRevenue,
			Double avgRating);


	// ===================== SUMMARY =====================

	@BeanMapping(ignoreByDefault = true)
	@Mappings({
	    @Mapping(target = "id", source = "id"),
			@Mapping(target = "name", source = "name") })
	RestaurantSummaryDto toSummaryDto(Restaurant restaurant);

	List<RestaurantSummaryDto> toSummaryDtoList(List<Restaurant> restaurants);
}