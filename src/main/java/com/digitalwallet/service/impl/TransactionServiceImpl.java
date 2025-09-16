package com.digitalwallet.service.impl;

import com.digitalwallet.dto.*;
import com.digitalwallet.entity.*;
import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.exception.InvalidTransactionException;
import com.digitalwallet.repository.TransactionRepository;
import com.digitalwallet.repository.WalletRepository;
import com.digitalwallet.service.NotificationService;
import com.digitalwallet.service.TransactionService;
import com.digitalwallet.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public TransactionResponse addMoney(Long userId, AddMoneyRequest request) {
        try {
//            Get user's wallet
            Wallet wallet = walletService.getWalletByUserId(userId);
            BigDecimal oldBalance = wallet.getBalance();

//            validate amount
            if(request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Amount must be greater than zero");
            }

//            Create transaction record
            Transaction transaction = new Transaction();
            transaction.setTransactionId(generateTransactionId());
            transaction.setReceiverWallet(wallet);
            transaction.setSenderWallet(null);
            transaction.setAmount(request.getAmount());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setDescription(request.getDescription());

//            Save transaction
            Transaction savedTransaction = transactionRepository.save(transaction);

//            update wallet balance
            BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

//            Mark transaction as  completed
            savedTransaction.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(savedTransaction);

//            Send real-time notification
            sendBalanceUpdateNotification(userId, wallet.getWalletNumber(),
                    oldBalance, newBalance, savedTransaction.getTransactionId(), "ADD_MONEY");

            sendTransactionNotification(userId, "Money Added",
                    "Successfully added" + request.getAmount() + "to your wallet",
                    savedTransaction);

            return convertToTransactionResponse(savedTransaction);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to add Money: " + e.getMessage());
        }
    }

    @Override
    public TransactionResponse transferMoney(Long senderUserId, MoneyTransferRequest request) {
        try {

            // Add null checks at the beginning
            if (request == null) {
                throw new RuntimeException("Transfer request cannot be null");
            }

            if (request.getAmount() == null) {
                throw new RuntimeException("Transfer amount cannot be null");
            }

            if (request.getReceiverWalletNumber() == null || request.getReceiverWalletNumber().trim().isEmpty()) {
                throw new RuntimeException("Receiver wallet number cannot be null or empty");
            }

            // Debug logging
            System.out.println("Processing transfer - Amount: " + request.getAmount());
            System.out.println("Processing transfer - Receiver: " + request.getReceiverWalletNumber());

//            get sender wallet
            Wallet senderWallet = walletService.getWalletByUserId(senderUserId);

//            get receiver wallet
            Wallet receiverWallet = walletService.getWalletByWalletNumber(request.getReceiverWalletNumber());

            BigDecimal senderOldBalance = senderWallet.getBalance();
            BigDecimal receiverOldBalance = receiverWallet.getBalance();

//            validate transfer
            validateTransfer(senderWallet, receiverWallet, request.getAmount());

//            create transaction record
            Transaction transaction = new Transaction();
            transaction.setTransactionId(generateTransactionId());
            transaction.setSenderWallet(senderWallet);
            transaction.setReceiverWallet(receiverWallet);
            transaction.setAmount(request.getAmount());
            transaction.setType(TransactionType.TRANSFER_SEND);
            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction.setDescription(request.getDescription());

//            Save the transaction
            Transaction savedTransaction = transactionRepository.save(transaction);

//            Update the balance
            BigDecimal senderNewBalance = senderWallet.getBalance().subtract(request.getAmount());
            BigDecimal receiverNewBalance = receiverWallet.getBalance().add(request.getAmount());

            senderWallet.setBalance(senderNewBalance);
            receiverWallet.setBalance(receiverNewBalance);

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

//            Create receiver transaction record
            Transaction receiverTransaction = new Transaction();
            receiverTransaction.setTransactionId(savedTransaction.getTransactionId() + "REC");
            receiverTransaction.setSenderWallet(senderWallet);
            receiverTransaction.setReceiverWallet(receiverWallet);
            receiverTransaction.setAmount(request.getAmount());
            receiverTransaction.setType(TransactionType.TRANSFER_RECEIVE);
            receiverTransaction.setStatus(TransactionStatus.COMPLETED);
            receiverTransaction.setDescription("Money received from " +
                    senderWallet.getUser().getFirstName() + " " + senderWallet.getUser().getLastName());

            transactionRepository.save(receiverTransaction);

//            Mark sender transaction as completed
            savedTransaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(savedTransaction);

//            Send real-time notification to sender
            sendBalanceUpdateNotification(senderUserId, senderWallet.getWalletNumber(),
                    senderOldBalance, senderNewBalance, savedTransaction.getTransactionId(), "TRANSFER_SEND");

            sendTransactionNotification(senderUserId, "Money Sent",
                    "Successfully sent " + request.getAmount() + " to " + receiverWallet.getWalletNumber(),
                    savedTransaction);

//            Send real-time notifications to receiver
            Long receiverUserId = receiverWallet.getUser().getId();
            sendBalanceUpdateNotification(receiverUserId, receiverWallet.getWalletNumber(),
                    receiverOldBalance, receiverNewBalance, savedTransaction.getTransactionId(), "TRANSFER_RECEIVE");

            sendTransactionNotification(receiverUserId, "Money Received",
                    "Received" + request.getAmount() + " from " +
                    senderWallet.getUser().getFirstName() + " " + senderWallet.getUser().getLastName(),
                    receiverTransaction);

            return convertToTransactionResponse(savedTransaction);

        } catch (Exception e) {
            throw new RuntimeException("Failed to transfer money: " + e.getMessage());
        }
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return convertToTransactionResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        Page<Transaction> transactions = transactionRepository.findByWallet(wallet, pageable);

        return transactions.map(this:: convertToTransactionResponse);
    }

    @Override
    public List<TransactionResponse> getRecentTransactions(Long userId, int limit) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        Pageable pageable = PageRequest.of(0, limit);
        Page<Transaction> transactions = transactionRepository.findByWallet(wallet, pageable);

        return transactions.getContent().stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

//    Helper Methods
    private void validateTransfer(Wallet senderWallet, Wallet receiverWallet, BigDecimal amount) {
        // Add null checks
        if (senderWallet == null) {
            throw new RuntimeException("Sender wallet not found");
        }

        if (receiverWallet == null) {
            throw new RuntimeException("Receiver wallet not found");
        }

        if (amount == null) {
            throw new RuntimeException("Transfer amount cannot be null");
        }

//        Check if amount is positive
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be greater than zero");
        }

//        Check if sender has sufficient balance
        if(senderWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + senderWallet.getBalance() + ", Required: " + amount);
        }

//        Check if sender and receiver are different
        if(senderWallet.getId().equals(receiverWallet.getId())) {
            throw new InvalidTransactionException("Cannot transfer money to your own wallet");
        }

//        Check wallet statuses
        if(senderWallet.getStatus() != WalletStatus.ACTIVE) {
            throw  new InvalidTransactionException("Sender wallet is not active");
        }

        if(receiverWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Receiver wallet is not active");
        }
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        String senderWalletNumber = transaction.getSenderWallet() != null ?
                transaction.getSenderWallet().getWalletNumber() : null;
        String receiverWalletNumber = transaction.getReceiverWallet() != null ?
                transaction.getReceiverWallet().getWalletNumber() : null;

        return new TransactionResponse(
                transaction.getTransactionId(),
                senderWalletNumber,
                receiverWalletNumber,
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    private void sendBalanceUpdateNotification(Long userId, String walletNumber,
                                               BigDecimal oldBalance, BigDecimal newBalance,
                                               String transactionId, String operation) {
        BalanceUpdateMessage balanceUpdate = new BalanceUpdateMessage(
                userId, walletNumber, oldBalance, newBalance, transactionId, operation);
        notificationService.sendBalanceUpdate(userId, balanceUpdate);
    }

    private void sendTransactionNotification(Long userId, String title, String message, Transaction transaction) {
        NotificationMessage notification = new NotificationMessage(
                "TRANSACTION", title, message, convertToTransactionResponse(transaction));
        notificationService.sendTransactionNotification(userId, notification);
    }

}
