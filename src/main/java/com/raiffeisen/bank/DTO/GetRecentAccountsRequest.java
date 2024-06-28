package com.raiffeisen.bank.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GetRecentAccountsRequest {

    @NotNull
    Long clientID;
    
    @Positive
    Integer limit;
}
