package com.digitalwallet.dto;

import com.digitalwallet.entity.TransactionStatus;
import com.digitalwallet.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionResponse {

    private  String transactionId;
    private String senderWalletNumber;
    private String receiverWalletNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
}
