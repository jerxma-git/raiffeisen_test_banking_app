package com.raiffeisen.bank.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientDTO {
    Long id;

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @Email
    String email;
}
