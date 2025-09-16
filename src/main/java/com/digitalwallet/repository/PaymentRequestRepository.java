package com.digitalwallet.repository;

import com.digitalwallet.entity.PaymentRequest;
import com.digitalwallet.entity.PaymentRequestStatus;
import com.digitalwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRequestRepository  extends JpaRepository<PaymentRequest, Long> {

    Optional<PaymentRequest> findByRequestId(String requestId);

    List<PaymentRequest> findByMerchantWalletOrderByCreatedAtDesc(Wallet merchantWallet);
    List<PaymentRequest> findByMerchantWalletAndStatusOrderByCreatedAtDesc(Wallet merchantWallet, PaymentRequestStatus status);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = 'PENDING' AND pr.expiresAt < :currentTime")
    List<PaymentRequest> findExpiredPaymentRequests(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(pr) FROM PaymentRequest pr WHERE pr.merchantWallet = :wallet AND pr.status = 'PAID'")
    Long countPaidRequestsByWallet(@Param("wallet") Wallet wallet);

}
