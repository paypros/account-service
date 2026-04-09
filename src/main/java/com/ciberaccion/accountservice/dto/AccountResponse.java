package com.ciberaccion.accountservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String merchantId;
    private BigDecimal balance;
    private String currency;
}