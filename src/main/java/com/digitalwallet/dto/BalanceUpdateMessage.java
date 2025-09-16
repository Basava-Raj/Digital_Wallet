package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceUpdateMessage {
    private Long userId;
    private String walletNumber;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private String transactionId;
    private String operation;
}
