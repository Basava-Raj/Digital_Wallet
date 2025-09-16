package com.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddMoneyRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Minimum amount is 0.01")
    private BigDecimal amount;

    private String description = "Money added to wallet";
}
