package com.raiffeisen.bank.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetAccountByAccountNumberRequest {
    @NotBlank
    String accountNumber;
}
