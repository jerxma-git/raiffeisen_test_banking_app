package com.raiffeisen.bank.DTO;

import java.time.LocalDateTime;

import com.raiffeisen.bank.models.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDTO {
    Long id;

    @NotNull
    Long clientID;

    String accountNumber;
    
    Double balance;

    AccountStatus status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

}
