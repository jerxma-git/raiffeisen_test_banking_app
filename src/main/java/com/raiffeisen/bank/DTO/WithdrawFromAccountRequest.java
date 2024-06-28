package com.raiffeisen.bank.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class WithdrawFromAccountRequest {
    @NotBlank
    String accountNumber;
    
    @PositiveOrZero
    @NotNull
    Double amount;
}
