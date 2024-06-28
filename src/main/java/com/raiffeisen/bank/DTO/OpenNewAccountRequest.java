package com.raiffeisen.bank.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpenNewAccountRequest {
    @NotNull
    Long clientID;
}
