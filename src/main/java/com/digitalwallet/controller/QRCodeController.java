// QRCodeController.java
package com.digitalwallet.controller;

import com.digitalwallet.dto.ApiResponse;
import com.digitalwallet.dto.QRPaymentRequest;
import com.digitalwallet.dto.QRPaymentResponse;
import com.digitalwallet.dto.TransactionResponse;
import com.digitalwallet.entity.PaymentRequest;
import com.digitalwallet.repository.PaymentRequestRepository;
import com.digitalwallet.service.PaymentService;
import com.digitalwallet.service.QRCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@Validated
@CrossOrigin(origins = "*")
public class QRCodeController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @PostMapping("/generate-payment/{merchantUserId}")
    public ResponseEntity<ApiResponse<QRPaymentResponse>> generatePaymentQR(
            @PathVariable Long merchantUserId,
            @Valid @RequestBody QRPaymentRequest request) {

        try {
            QRPaymentResponse qrResponse = paymentService.generatePaymentQR(merchantUserId, request);
            ApiResponse<QRPaymentResponse> response = ApiResponse.success(
                    "Payment QR generated successfully", qrResponse);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            ApiResponse<QRPaymentResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/payment-request/{requestId}")
    public ResponseEntity<ApiResponse<QRPaymentResponse>> getPaymentRequest(@PathVariable String requestId) {
        try {
            QRPaymentResponse qrResponse = paymentService.getPaymentRequest(requestId);
            ApiResponse<QRPaymentResponse> response = ApiResponse.success(qrResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<QRPaymentResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/pay/{payerUserId}/{requestId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> payViaQR(
            @PathVariable Long payerUserId,
            @PathVariable String requestId) {

        try {
            TransactionResponse transaction = paymentService.payViaQR(payerUserId, requestId);
            ApiResponse<TransactionResponse> response = ApiResponse.success(
                    "Payment completed successfully", transaction);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<TransactionResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/wallet-qr/{userId}")
    public ResponseEntity<ApiResponse<String>> generateWalletQR(@PathVariable Long userId) {
        try {
            String qrData = paymentService.generateWalletQR(userId);
            ApiResponse<String> response = ApiResponse.success("Wallet QR generated", qrData);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/image/{requestId}")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String requestId) {
        try {
            QRPaymentResponse paymentRequest = paymentService.getPaymentRequest(requestId);
            byte[] qrCodeImage = Base64.getDecoder().decode(paymentRequest.getQrCodeImageBase64());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);

            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/expire-old")
    public ResponseEntity<ApiResponse<String>> expireOldRequests() {
        try {
            paymentService.expireOldPaymentRequests();
            ApiResponse<String> response = ApiResponse.success("Old payment requests expired successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Add this method to QRCodeController.java for debugging
    @GetMapping("/debug/payment-request/{requestId}")
    public ResponseEntity<ApiResponse<Object>> debugPaymentRequest(@PathVariable String requestId) {
        try {
            PaymentRequest paymentRequest = paymentRequestRepository.findByRequestId(requestId)
                    .orElseThrow(() -> new RuntimeException("Payment request not found"));

            // Create debug info
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("id", paymentRequest.getId());
            debugInfo.put("requestId", paymentRequest.getRequestId());
            debugInfo.put("amount", paymentRequest.getAmount());
            debugInfo.put("amountIsNull", paymentRequest.getAmount() == null);
            debugInfo.put("description", paymentRequest.getDescription());
            debugInfo.put("status", paymentRequest.getStatus());
            debugInfo.put("merchantWalletId", paymentRequest.getMerchantWallet().getId());
            debugInfo.put("merchantWalletNumber", paymentRequest.getMerchantWallet().getWalletNumber());

            ApiResponse<Object> response = ApiResponse.success("Debug info", debugInfo);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<Object> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

}
