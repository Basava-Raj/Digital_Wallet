package com.digitalwallet.exception;

public class PaymentRequestExpiredException extends RuntimeException {
    public PaymentRequestExpiredException(String message) {
        super(message);
    }
}
