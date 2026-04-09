package com.ciberaccion.accountservice.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String merchantId) {
        super("Account not found for merchant: " + merchantId);
    }
}