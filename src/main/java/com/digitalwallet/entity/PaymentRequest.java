package com.digitalwallet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.sql.results.graph.collection.internal.BagInitializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_requests")
@NoArgsConstructor
@Data
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String requestId;

    @ManyToOne
    @JoinColumn(name = "merchant_wallet_id", nullable = false)
    private Wallet merchantWallet;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private PaymentRequestStatus status = PaymentRequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String qrCodeData;

    @ManyToOne
    @JoinColumn(name = "paid_by_wallet_id")
    private Wallet paidByWallet;

    @Column
    private String transactionId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime expiresAt;

}
