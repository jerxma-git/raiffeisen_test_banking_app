package com.raiffeisen.bank.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raiffeisen.bank.models.Account;
import com.raiffeisen.bank.services.AccountService;


@RestController
@RequestMapping("/api/accounts")
public class AccountController {

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
        String accountNUmber = requestBody.get("accountNumber");
        boolean isSuccessful = accountService.closeAccountByAccountNumber(accountNUmber);
        if (!isSuccessful) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("Account closed successfully.");
    }
}
