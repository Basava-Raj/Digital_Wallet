package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QRPaymentResponse {
    private String requestId;
    private String qrCodeData;
    private String qrCodeImageBase64;
    private BigDecimal amount;
    private String description;
    private String merchantWalletNumber;
    private LocalDateTime expiresAt;
    private String status;
}
