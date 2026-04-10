package com.ciberaccion.accountservice.service;

import com.ciberaccion.accountservice.dto.*;
import com.ciberaccion.accountservice.exception.AccountNotFoundException;
import com.ciberaccion.accountservice.model.Account;
import com.ciberaccion.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse getAccount(String merchantId) {
        Account account = accountRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new AccountNotFoundException(merchantId));
        return toResponse(account);
    }

    @Transactional
    public AccountResponse debit(String merchantId, DebitRequest request) {
        Account account = accountRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new AccountNotFoundException(merchantId));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        return toResponse(account);
    }

    public AccountResponse save(Account account) {
        return toResponse(accountRepository.save(account));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getMerchantId(),
                account.getBalance(),
                account.getCurrency());
    }
}