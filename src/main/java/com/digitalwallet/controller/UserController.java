package com.digitalwallet.controller;

import com.digitalwallet.dto.ApiResponse;
import com.digitalwallet.dto.UserRegistrationRequest;
import com.digitalwallet.dto.UserResponse;
import com.digitalwallet.entity.User;
import com.digitalwallet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Validated
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        try {
            UserResponse userResponse = userService.registerUser(request);
            ApiResponse<UserResponse> response = ApiResponse.success(
                    "User registered successfully", userResponse);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        }
        catch (RuntimeException e) {
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        try {
            UserResponse userResponse = userService.getUserById(userId);
            ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
            return  new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (RuntimeException e) {
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        try {
            UserResponse userResponse = userService.getUserByEmail(email);
            ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            ApiResponse<UserResponse> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}
