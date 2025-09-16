package com.digitalwallet.service;

import com.digitalwallet.dto.UserRegistrationRequest;
import com.digitalwallet.dto.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRegistrationRequest request);
    UserResponse getUserById(Long userId);
    UserResponse getUserByEmail(String email);

}
