package com.raiffeisen.bank.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raiffeisen.bank.models.Account;
import com.raiffeisen.bank.models.Client;
import com.raiffeisen.bank.services.AccountService;

import jakarta.websocket.server.PathParam;


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
    public ResponseEntity<Account> openNewAccount(@RequestBody Map<String, Long> requestBody) {
        if (!requestBody.containsKey("clientID")) {
            return ResponseEntity.badRequest().build();
        }
        Long clientID = requestBody.get("clientID");
        Account opened = accountService.openNewAccount(clientID);
        if (opened == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(opened);
    }

    @PostMapping("/close")
    public ResponseEntity<String> closeAccount(@RequestBody Map<String, String> requestBody) {
        if (!requestBody.containsKey("accountNumber")) {
            return ResponseEntity.badRequest().build();
        }
        String accountNumber = requestBody.get("accountNumber");
        boolean isSuccessful = accountService.closeAccountByAccountNumber(accountNumber);
        if (!isSuccessful) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("Account closed successfully.");
    }


    @PutMapping("/deposit")
    public ResponseEntity<String> depositToAccount(@RequestBody Map<String, Object> requestBody) {
        String accountNumber = (String) requestBody.getOrDefault("accountNumber", null);
        Double amount = (Double) requestBody.getOrDefault("amount", null);
        
        if (accountNumber == null || amount == null) {
            return ResponseEntity.badRequest().body("Missing arguments");
        }
    
        boolean isSuccessful = accountService.applyAccountBalanceDelta(accountNumber, amount);
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account with the provided account number doesn't exist");
        }

        return ResponseEntity.ok("Deposit successful");
    }

    @PutMapping("/withdraw")
    public ResponseEntity<String> withdrawFromAccount(@RequestBody Map<String, Object> requestBody) {
        String accountNumber = (String) requestBody.getOrDefault("accountNumber", null);
        Double amount = (Double) requestBody.getOrDefault("amount", null);
        
        if (accountNumber == null || amount == null) {
            return ResponseEntity.badRequest().body("Missing arguments");
        }
        
        boolean isSuccessful = accountService.applyAccountBalanceDelta(accountNumber, -amount);
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account with the provided account number doesn't exist");
        }

        return ResponseEntity.ok("Withdrawal successful");
    }




    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(account);
    }

    @GetMapping("/by_number")
    public ResponseEntity<Account> getAccountByAccountNumber(@RequestBody Map<String, String> requestBody) {
        if (!requestBody.containsKey("accountNumber")) {
            return ResponseEntity.badRequest().build();
        }
        String accountNumber = requestBody.get("accountNumber");
        Account account = accountService.getAccountByAccountNumber(accountNumber);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(account);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Account>> getRecentAccounts(@RequestBody Map<String, Object> requestBody) {
        if (!requestBody.containsKey("clientID")) {
            return ResponseEntity.badRequest().build();
        }
        Long clientID = ((Integer) requestBody.get("clientID")).longValue();
        int limit = requestBody.containsKey("limit") ? (int) requestBody.get("limit") : DEFAULT_RECENTS_LIMIT;

        return ResponseEntity.ok(accountService.getRecentAccounts(clientID, limit));
    }

    
}
