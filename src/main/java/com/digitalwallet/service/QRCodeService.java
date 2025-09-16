package com.digitalwallet.service;

import java.math.BigDecimal;

public interface QRCodeService {
    String generatePaymentQR(String walletNumber, BigDecimal amount, String description);
    String generateWalletQR(String walletNumber);
    byte[] generateQRCodeImage(String qrData, int width, int height);

}
