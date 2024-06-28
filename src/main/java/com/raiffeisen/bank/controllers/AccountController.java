package com.raiffeisen.bank.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raiffeisen.bank.DTO.AccountDTO;
import com.raiffeisen.bank.DTO.CloseAccountRequest;
import com.raiffeisen.bank.DTO.DepositToAccountRequest;
import com.raiffeisen.bank.DTO.GetAccountByAccountNumberRequest;
import com.raiffeisen.bank.DTO.GetRecentAccountsRequest;
import com.raiffeisen.bank.DTO.OpenNewAccountRequest;
import com.raiffeisen.bank.DTO.WithdrawFromAccountRequest;
import com.raiffeisen.bank.services.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final int DEFAULT_RECENTS_LIMIT = 5;
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/open")
    public ResponseEntity<AccountDTO> openNewAccount(@RequestBody @Valid OpenNewAccountRequest r) {

        AccountDTO opened = accountService.openNewAccount(r.getClientID());
        if (opened == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(opened);
    }

    @PostMapping("/close")
    public ResponseEntity<String> closeAccount(@RequestBody @Valid CloseAccountRequest r) {
        boolean isSuccessful = accountService.closeAccountByAccountNumber(r.getAccountNumber());
        if (!isSuccessful) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Account closed successfully.");
    }

    @PutMapping("/deposit")
    public ResponseEntity<String> depositToAccount(@RequestBody @Valid DepositToAccountRequest r) {

        boolean isSuccessful = accountService.applyAccountBalanceDelta(r.getAccountNumber(), r.getAmount());
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Account with the provided account number doesn't exist");
        }

        return ResponseEntity.ok("Deposit successful");
    }

    @PutMapping("/withdraw")
    public ResponseEntity<String> withdrawFromAccount(@RequestBody @Valid WithdrawFromAccountRequest r) {
        boolean isSuccessful = accountService.applyAccountBalanceDelta(r.getAccountNumber(), -r.getAmount());
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Account with the provided account number doesn't exist");
        }

        return ResponseEntity.ok("Withdrawal successful");
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        AccountDTO account = accountService.getAccountDTOById(id);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(account);
    }

    @GetMapping("/by_number")
    public ResponseEntity<AccountDTO> getAccountByAccountNumber(@RequestBody @Valid GetAccountByAccountNumberRequest r) {
        AccountDTO account = accountService.getAccountDTOByAccountNumber(r.getAccountNumber());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(account);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AccountDTO>> getRecentAccounts(@RequestBody @Valid GetRecentAccountsRequest r) {

        int limit = r.getLimit() != null ? r.getLimit() : DEFAULT_RECENTS_LIMIT;

        return ResponseEntity.ok(accountService.getRecentAccounts(r.getClientID(), limit));
    }

}
