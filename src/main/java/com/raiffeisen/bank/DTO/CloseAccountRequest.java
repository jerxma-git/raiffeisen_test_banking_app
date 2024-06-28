package com.raiffeisen.bank.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseAccountRequest {
    @NotBlank
    String accountNumber;
}
