package com.digitalwallet.service.impl;

import com.digitalwallet.dto.MoneyTransferRequest;
import com.digitalwallet.dto.QRPaymentRequest;
import com.digitalwallet.dto.QRPaymentResponse;
import com.digitalwallet.dto.TransactionResponse;
import com.digitalwallet.entity.PaymentRequest;
import com.digitalwallet.entity.PaymentRequestStatus;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.repository.PaymentRequestRepository;
import com.digitalwallet.service.PaymentService;
import com.digitalwallet.service.QRCodeService;
import com.digitalwallet.service.TransactionService;
import com.digitalwallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Override
    public QRPaymentResponse generatePaymentQR(Long merchantId, QRPaymentRequest request) {
        try {
//            Get merchant wallet
            Wallet merchantWallet = walletService.getWalletByUserId(merchantId);

//            Create payment request
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setRequestId(generateRequestId());
            paymentRequest.setMerchantWallet(merchantWallet);
            paymentRequest.setAmount(request.getAmount());
            paymentRequest.setDescription(request.getDescription());
            paymentRequest.setStatus(PaymentRequestStatus.PENDING);
            paymentRequest.setExpiresAt(LocalDateTime.now().plusMinutes(request.getValidityMinutes()));

//            Generate QR code data
            String qrData = qrCodeService.generatePaymentQR(
                    merchantWallet.getWalletNumber(),
                    request.getAmount(),
                    request.getDescription()
            );

//            Add request ID to QR data
            qrData = qrData.replace("{", ",\"requestID\":\"" + paymentRequest.getRequestId() + "\"}");
            paymentRequest.setQrCodeData(qrData);

//            Save payment request
            PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);

//            Generate QR code image
            byte[] qrCodeImage = qrCodeService.generateQRCodeImage(qrData, 300, 300);
            String qrCodeImageBase64 = Base64.getEncoder().encodeToString(qrCodeImage);

            return new QRPaymentResponse(
                    savedRequest.getRequestId(),
                    qrData,
                    qrCodeImageBase64,
                    savedRequest.getAmount(),
                    savedRequest.getDescription(),
                    merchantWallet.getWalletNumber(),
                    savedRequest.getExpiresAt(),
                    savedRequest.getStatus().toString()
            );
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to generate payment QR: " + e.getMessage());
        }
    }

    @Override
    public QRPaymentResponse getPaymentRequest(String requestId) {
        PaymentRequest paymentRequest = paymentRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Payment request not found"));

//        Check if expired
        if(paymentRequest.getStatus() == PaymentRequestStatus.PENDING &&
            paymentRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            paymentRequest.setStatus(PaymentRequestStatus.EXPIRED);
            paymentRequestRepository.save(paymentRequest);

        }

//        Generate QR code image if needed
        byte[] qrCodeImage = qrCodeService.generateQRCodeImage(paymentRequest.getQrCodeData(), 300, 300);
        String qrCodeImageBase64 = Base64.getEncoder().encodeToString(qrCodeImage);

        return new QRPaymentResponse(
                paymentRequest.getRequestId(),
                paymentRequest.getQrCodeData(),
                qrCodeImageBase64,
                paymentRequest.getAmount(),
                paymentRequest.getDescription(),
                paymentRequest.getMerchantWallet().getWalletNumber(),
                paymentRequest.getExpiresAt(),
                paymentRequest.getStatus().toString()
        );

    }

    @Override
    public TransactionResponse payViaQR(Long payerUserId, String requestId) {
        try {
//            Get payment request
            PaymentRequest paymentRequest = paymentRequestRepository.findByRequestId(requestId)
                    .orElseThrow(() -> new RuntimeException("Payment request not found"));

//            validate payment request
            validatePaymentRequest(paymentRequest);

//            Get payer wallet
            Wallet payerWallet = walletService.getWalletByUserId(payerUserId);

//            Prevent self-payment
            if(payerWallet.getId().equals(paymentRequest.getMerchantWallet().getId())) {
                throw new RuntimeException("Cannot pay to your own QR code");
            }

            // Check if payment request amount is not null
            if (paymentRequest.getAmount() == null) {
                throw new RuntimeException("Payment request amount is invalid");
            }

            // Create transfer request with proper null checks
            MoneyTransferRequest transferRequest = new MoneyTransferRequest();
            transferRequest.setReceiverWalletNumber(paymentRequest.getMerchantWallet().getWalletNumber());
            transferRequest.setAmount(paymentRequest.getAmount()); // This should not be null

            String description = "QR Payment";
            if (paymentRequest.getDescription() != null && !paymentRequest.getDescription().trim().isEmpty()) {
                description += ": " + paymentRequest.getDescription();
            }
            transferRequest.setDescription(description);

            // Debug logging
            System.out.println("Transfer Request - Amount: " + transferRequest.getAmount());
            System.out.println("Transfer Request - Receiver: " + transferRequest.getReceiverWalletNumber());
            System.out.println("Transfer Request - Description: " + transferRequest.getDescription());

//          Execute transfer
            TransactionResponse transactionResponse = transactionService.transferMoney(payerUserId, transferRequest);

//          update payment request
            paymentRequest.setStatus(PaymentRequestStatus.PAID);
            paymentRequest.setPaidByWallet(payerWallet);
            paymentRequest.setTransactionId(transactionResponse.getTransactionId());
            paymentRequest.setPaidAt(LocalDateTime.now());
            paymentRequestRepository.save(paymentRequest);

            return transactionResponse;

        }
        catch (Exception e) {
            throw new RuntimeException("Failed to process QR payment: " + e.getMessage());
        }
    }

    @Override
    public String generateWalletQR(Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return qrCodeService.generateWalletQR(wallet.getWalletNumber());
    }

    @Override
    public void expireOldPaymentRequests() {
        List<PaymentRequest> expiredRequests = paymentRequestRepository
                .findExpiredPaymentRequests(LocalDateTime.now());

        for (PaymentRequest request : expiredRequests) {
            request.setStatus(PaymentRequestStatus.EXPIRED);
        }

        if (!expiredRequests.isEmpty()) {
            paymentRequestRepository.saveAll(expiredRequests);
        }
    }

//    Helper methods
    private void validatePaymentRequest(PaymentRequest paymentRequest) {
        if(paymentRequest.getStatus() != PaymentRequestStatus.PENDING) {
            throw new RuntimeException("Payment request is not available for payment. Status: " +
                    paymentRequest.getStatus());
        }

        if(paymentRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            paymentRequest.setStatus(PaymentRequestStatus.EXPIRED);
            paymentRequestRepository.save(paymentRequest);
            throw new RuntimeException("Payment request has expired");
        }
    }

    private String generateRequestId() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0,12).toUpperCase();
    }


}
