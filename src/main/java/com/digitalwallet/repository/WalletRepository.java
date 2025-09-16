package com.digitalwallet.repository;

import com.digitalwallet.entity.User;
import com.digitalwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
    Optional<Wallet> findByWalletNumber(String walletNumber);
    boolean existsByWalletNumber(String walletNumber);
}
