package com.digitalwallet.controller;

import com.digitalwallet.dto.*;
import com.digitalwallet.service.NotificationService;
import com.digitalwallet.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Validated
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/add-money/{userId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> addMoney(
            @PathVariable Long userId,
            @Valid @RequestBody AddMoneyRequest request) {

        try {
            TransactionResponse transaction = transactionService.addMoney(userId, request);
            ApiResponse<TransactionResponse> response = ApiResponse.success(
                    "Money added successfully", transaction);
            return  new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            ApiResponse<TransactionResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/transfer/{senderUserId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @PathVariable Long senderUserId,
            @Valid @RequestBody MoneyTransferRequest request) {

        try {
            TransactionResponse transaction = transactionService.transferMoney(senderUserId, request);
            ApiResponse<TransactionResponse> response = ApiResponse.success(
                    "Money transferred successfully", transaction);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (RuntimeException e) {
            ApiResponse<TransactionResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{transactionID}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String transactionId) {

        try {
            TransactionResponse transaction = transactionService.getTransactionById(transactionId);
            ApiResponse<TransactionResponse> response = ApiResponse.success(transaction);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<TransactionResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TransactionResponse> transactions = transactionService.getTransactionHistory(userId, pageable);
            ApiResponse<Page<TransactionResponse>> response = ApiResponse.success(transactions);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<Page<TransactionResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            List<TransactionResponse> transactions = transactionService.getRecentTransactions(userId, limit);
            ApiResponse<List<TransactionResponse>> response = ApiResponse.success(
                    "Recent transactions retrieved", transactions);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<TransactionResponse>> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/test-notification/{userId}")
    public ResponseEntity<ApiResponse<String>> testNotification(
            @PathVariable Long userId,
            @RequestBody(required = false) String message) {

        try {
            String notificationMessage = message != null ? message : "Test notification for user " + userId;

            NotificationMessage notification = new NotificationMessage(
                    "TEST",
                    "Test Notification",
                    notificationMessage,
                    System.currentTimeMillis()
            );

            notificationService.sendTransactionNotification(userId, notification);

            ApiResponse<String> response = ApiResponse.success(
                    "Test notification sent successfully",
                    "Notification sent to user " + userId
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

@PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<String>> broadcastMessage(@RequestBody String message) {
        try {
            NotificationMessage notification = new NotificationMessage(
                    "BROADCAST",
                    "System Announcement",
                    message,
                    System.currentTimeMillis()
            );

            notificationService.sendGlobalNotification(notification);
            ApiResponse<String> response = ApiResponse.success(
                    "Broadcast message sent successfully",
                    "Message broadcasted to all users"
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
