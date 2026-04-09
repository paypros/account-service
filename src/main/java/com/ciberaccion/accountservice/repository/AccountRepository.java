package com.ciberaccion.accountservice.repository;

import com.ciberaccion.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByMerchantId(String merchantId);

}
