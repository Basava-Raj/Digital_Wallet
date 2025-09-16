package com.digitalwallet.service.impl;

import com.digitalwallet.entity.User;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.exception.WalletNotFoundException;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.repository.WalletRepository;
import com.digitalwallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Wallet getWalletByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WalletNotFoundException("User not found"));

        return walletRepository.findByUser(user)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    @Override
    public Wallet getWalletByWalletNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with number: " + walletNumber));
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet.getBalance();
    }
}
