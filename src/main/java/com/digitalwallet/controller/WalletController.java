// WalletController.java - Updated version
package com.digitalwallet.controller;

import com.digitalwallet.dto.ApiResponse;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Wallet>> getWalletByUserId(@PathVariable Long userId) {
        try {
            Wallet wallet = walletService.getWalletByUserId(userId);
            ApiResponse<Wallet> response = ApiResponse.success(wallet);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<Wallet> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable Long userId) {
        try {
            BigDecimal balance = walletService.getBalance(userId);
            ApiResponse<BigDecimal> response = ApiResponse.success("Current balance", balance);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<BigDecimal> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/number/{walletNumber}")
    public ResponseEntity<ApiResponse<Wallet>> getWalletByNumber(@PathVariable String walletNumber) {
        try {
            Wallet wallet = walletService.getWalletByWalletNumber(walletNumber);
            ApiResponse<Wallet> response = ApiResponse.success(wallet);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<Wallet> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }


}
