package com.ciberaccion.accountservice.controller;

import com.ciberaccion.accountservice.dto.*;
import com.ciberaccion.accountservice.model.Account;
import com.ciberaccion.accountservice.service.AccountService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    public ResponseEntity<List<AccountResponse>> getAccounts() {
        return ResponseEntity.ok(accountService.getAccounts());
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

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountResponse request) {
        Account account = new Account();
        account.setMerchantId(request.getMerchantId());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency());
        return ResponseEntity.ok(accountService.save(account));
    }
}