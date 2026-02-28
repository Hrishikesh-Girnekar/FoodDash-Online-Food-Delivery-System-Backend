package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantSummaryDto {

    private Long id;
    private String name;
}
