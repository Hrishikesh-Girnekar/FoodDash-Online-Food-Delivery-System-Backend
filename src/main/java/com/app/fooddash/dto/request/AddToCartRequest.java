package com.app.fooddash.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {

    @NotNull
    private Long menuItemId;

    @NotNull
    private Integer quantity;
}
