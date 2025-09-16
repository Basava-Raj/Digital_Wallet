package com.digitalwallet.service;

import com.digitalwallet.dto.QRPaymentRequest;
import com.digitalwallet.dto.QRPaymentResponse;
import com.digitalwallet.dto.TransactionResponse;

public interface PaymentService {
    QRPaymentResponse generatePaymentQR(Long merchantId, QRPaymentRequest request);
    QRPaymentResponse getPaymentRequest(String requestId);
    TransactionResponse payViaQR(Long payerUserId, String requestId);
    String generateWalletQR(Long userID);
    void expireOldPaymentRequests();
}
