package com.digitalwallet.repository;

import com.digitalwallet.entity.Transaction;
import com.digitalwallet.entity.TransactionStatus;
import com.digitalwallet.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    Optional<Transaction> findByTransactionId(String transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.senderWallet = :wallet OR t.receiverWallet = :wallet ORDER BY t.createdAt DESC")
    Page<Transaction> findByWallet(@Param("wallet") Wallet wallet, Pageable pageable);

    @Query("SELECT t FROM Transaction t  WHERE (t.senderWallet = :wallet OR t.receiverWallet = :wallet) AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletAndStatus(@Param("wallet") Wallet wallet, @Param("status")TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE (t.senderWallet = :wallet OR t.receiverWallet = :wallet) AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletAndDateRange(@Param("wallet") Wallet wallet,
                                               @Param( "startDate")LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

}
