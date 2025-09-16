package com.digitalwallet.service.impl;

import com.digitalwallet.dto.UserRegistrationRequest;
import com.digitalwallet.dto.UserResponse;
import com.digitalwallet.entity.User;
import com.digitalwallet.entity.UserStatus;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.entity.WalletStatus;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.repository.WalletRepository;
import com.digitalwallet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse registerUser(UserRegistrationRequest request) {
//        Check if user already exists
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("user with this email already exists");
        }

        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("User with this phone number already exists");
        }

//        Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

//        Save user
        User savedUser = userRepository.save(user);

//      Create wallet for the user
        Wallet wallet = createWalletForUser(savedUser);

//        Return user response
        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                wallet.getWalletNumber(),
                wallet.getCreatedAt()
        );
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));

        return  new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                wallet.getWalletNumber(),
                user.getCreatedAt()
        );
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                wallet.getWalletNumber(),
                user.getCreatedAt()
        );
    }

    private Wallet createWalletForUser(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setWalletNumber(generateWalletNumber());
        wallet.setStatus(WalletStatus.ACTIVE);

        return walletRepository.save(wallet);
    }

    private String generateWalletNumber() {
        String walletNumber;
        do {
            walletNumber = "WLT" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (walletRepository.existsByWalletNumber(walletNumber));

        return walletNumber;
    }

}
