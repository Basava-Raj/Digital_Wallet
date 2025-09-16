package com.digitalwallet.service;

import com.digitalwallet.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    Wallet getWalletByUserId(Long userId);
    Wallet getWalletByWalletNumber(String walletNumber);
    BigDecimal getBalance(Long userId);
}
