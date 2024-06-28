package com.raiffeisen.bank;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.raiffeisen.bank.controllers.AccountController;
import com.raiffeisen.bank.models.Account;
import com.raiffeisen.bank.models.AccountStatus;
import com.raiffeisen.bank.models.Client;
import com.raiffeisen.bank.services.AccountService;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @MockBean
    AccountService accountService;

    Client sampleClient = Client.builder()
            .firstName("sample_firstName")
            .lastName("sample_lastName")
            .email("sample_email")
            .id(1L)
            .build();

    @Autowired
    public AccountControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(Include.NON_NULL)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    }

    @Test
    void testOpenNewAccount() throws Exception {
        Account freshAccount = Account.builder()
                .id(1L)
                .client(sampleClient)
                .status(AccountStatus.ACTIVE)
                .build();
        Mockito.when(accountService.openNewAccount(1L)).thenReturn(freshAccount);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/open")
                .contentType("application/json")
                .content("{\"clientID\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(freshAccount.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(freshAccount.getStatus().toString()));
    }

    @Test
    void testCloseAccount() throws Exception {
        Account closedAccount = Account.builder()
                .id(1L)
                .accountNumber("01234567899876543210")
                .client(sampleClient)
                .build();
        Mockito.when(accountService.closeAccountByAccountNumber(closedAccount.getAccountNumber()))
                .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/close")
                .contentType("application/json")
                .content("{\"accountNumber\": \"01234567899876543210\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testDepositToAccount() throws Exception {
        String accountNumber = "123456";
        double amount = 100.0;

        Map<String, Object> existingAccRequest = Map.of(
                "accountNumber", accountNumber,
                "amount", amount);

        Map<String, Object> nonexistentAccRequest = Map.of(
                "accountNumber", "kasjhdfklajshdfakshdfklasjdfhklasjdfh",
                "amount", amount);

        // Mockito.when(accountService.getAccountByAccountNumber(accountNumber)).thenReturn(existingAcc);
        // Mockito.when(accountService.getAccountByAccountNumber(argThat(accNum ->
        // !accNum.equals(accountNumber)))).thenReturn(null);

        Mockito.when(accountService.applyAccountBalanceDelta(eq(accountNumber), Mockito.anyDouble()))
                .thenReturn(true);

        Mockito.when(accountService.applyAccountBalanceDelta(argThat(accNum -> !accNum.equals(accountNumber)),
                Mockito.anyDouble()))
                .thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/accounts/deposit")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(existingAccRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Deposit successful"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/accounts/deposit")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(nonexistentAccRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
