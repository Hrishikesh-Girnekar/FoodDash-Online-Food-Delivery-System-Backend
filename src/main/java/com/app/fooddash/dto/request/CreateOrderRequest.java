package com.app.fooddash.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /** Amount in rupees (e.g. 499 for ₹499). Backend converts to paise. */
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least ₹1")
    private Integer amount;

    /** Optional: currency code. Defaults to INR. */
    private String currency = "INR";

    /** Optional: your internal reference (e.g. product ID) */
    private String receipt;
}