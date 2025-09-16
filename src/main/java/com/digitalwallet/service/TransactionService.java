package com.digitalwallet.service;

import com.digitalwallet.dto.AddMoneyRequest;
import com.digitalwallet.dto.MoneyTransferRequest;
import com.digitalwallet.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    TransactionResponse addMoney(Long userId, AddMoneyRequest request);
    TransactionResponse transferMoney(Long sendUserId, MoneyTransferRequest request);
    TransactionResponse getTransactionById(String transactionId);
    Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable);
    List<TransactionResponse> getRecentTransactions(Long userId, int limit);
}
