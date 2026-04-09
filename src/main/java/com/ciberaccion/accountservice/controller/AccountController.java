package com.ciberaccion.accountservice.controller;

import com.ciberaccion.accountservice.dto.*;
import com.ciberaccion.accountservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String merchantId) {
        return ResponseEntity.ok(accountService.getAccount(merchantId));
    }

    @PostMapping("/{merchantId}/debit")
    public ResponseEntity<AccountResponse> debit(
            @PathVariable String merchantId,
            @RequestBody DebitRequest request) {
        return ResponseEntity.ok(accountService.debit(merchantId, request));
    }
}